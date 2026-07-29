use std::{
    collections::{BTreeMap, BTreeSet},
    env,
    error::Error,
    fmt, fs,
    path::PathBuf,
};

const AUTH_ENDPOINTS: &[(&str, &str)] = &[
    ("LOGIN", "/login"),
    ("SIGNUP", "/signup"),
    ("REFRESH_TOKEN", "/auth/refresh"),
];

const AUTH_SCHEMAS: &[&str] = &[
    "ErrorMessageDto",
    "LoginRequest",
    "LoginResponse",
    "RefreshTokenRequest",
    "RefreshTokenResponse",
    "SignupRequest",
    "SignupResponse",
    "UserDto",
];

fn main() -> Result<(), Box<dyn Error>> {
    let manifest_dir = PathBuf::from(env::var("CARGO_MANIFEST_DIR")?);
    let schema_path = manifest_dir.join("../../backend/openapi-schema/openapi.yaml");

    println!("cargo:rerun-if-changed={}", schema_path.display());

    let schema = fs::read_to_string(&schema_path)?;
    let schemas = parse_schemas(&schema)?;
    let endpoints = parse_endpoints(&schema)?;
    let output = generate_code(&schemas, &endpoints)?;
    let out_dir = PathBuf::from(env::var("OUT_DIR")?);

    fs::write(out_dir.join("generated.rs"), output)?;

    Ok(())
}

#[derive(Debug)]
struct CodegenError(String);

impl fmt::Display for CodegenError {
    fn fmt(&self, formatter: &mut fmt::Formatter<'_>) -> fmt::Result {
        formatter.write_str(&self.0)
    }
}

impl Error for CodegenError {}

#[derive(Clone, Debug)]
struct Schema {
    name: String,
    properties: Vec<Property>,
}

#[derive(Clone, Debug)]
struct Property {
    name: String,
    kind: PropertyKind,
}

#[derive(Clone, Debug)]
enum PropertyKind {
    String,
    Integer,
    Number,
    Boolean,
    Array(Box<PropertyKind>),
    Ref(String),
    Optional(Box<PropertyKind>),
}

impl PropertyKind {
    fn optional(self) -> Self {
        if self.is_optional() {
            self
        } else {
            Self::Optional(Box::new(self))
        }
    }

    fn is_optional(&self) -> bool {
        matches!(self, Self::Optional(_))
    }

    fn rust_type(&self) -> String {
        match self {
            Self::String => "String".to_owned(),
            Self::Integer => "i64".to_owned(),
            Self::Number => "f64".to_owned(),
            Self::Boolean => "bool".to_owned(),
            Self::Array(inner) => format!("Vec<{}>", inner.rust_type()),
            Self::Ref(name) => name.clone(),
            Self::Optional(inner) => format!("Option<{}>", inner.rust_type()),
        }
    }
}

#[derive(Clone, Debug)]
struct Endpoint {
    const_name: String,
    method: String,
    path: String,
}

fn parse_schemas(schema: &str) -> Result<BTreeMap<String, Schema>, Box<dyn Error>> {
    let lines = schema.lines().collect::<Vec<_>>();
    let mut parsed = BTreeMap::new();

    for &name in AUTH_SCHEMAS {
        parsed.insert(name.to_owned(), parse_schema(&lines, name)?);
    }

    Ok(parsed)
}

fn parse_schema(lines: &[&str], schema_name: &str) -> Result<Schema, Box<dyn Error>> {
    let start = lines
        .iter()
        .position(|line| *line == format!("    {schema_name}:"))
        .ok_or_else(|| CodegenError(format!("schema `{schema_name}` is missing from OpenAPI")))?;

    let end = next_block_end(lines, start + 1, 4);
    let block = &lines[start + 1..end];
    let required = parse_required(block);
    let mut properties = Vec::new();
    let mut index = 0;

    while index < block.len() {
        let line = block[index];
        let trimmed = line.trim();

        if trimmed == "properties:" {
            index += 1;

            while index < block.len() {
                let line = block[index];
                let indent = leading_spaces(line);
                let trimmed = line.trim();

                if indent <= 6 && !trimmed.is_empty() {
                    break;
                }

                if indent == 8 && trimmed.ends_with(':') {
                    let property_name = trimmed.trim_end_matches(':');
                    let property_end = next_block_end(block, index + 1, 8);
                    let property_kind = parse_property_kind(&block[index + 1..property_end])?;
                    let property_kind = if required.contains(property_name) {
                        property_kind
                    } else {
                        property_kind.optional()
                    };

                    properties.push(Property {
                        name: property_name.to_owned(),
                        kind: property_kind,
                    });
                    index = property_end;
                    continue;
                }

                index += 1;
            }

            continue;
        }

        index += 1;
    }

    Ok(Schema {
        name: schema_name.to_owned(),
        properties,
    })
}

fn parse_required(block: &[&str]) -> BTreeSet<String> {
    let mut required = BTreeSet::new();
    let mut index = 0;

    while index < block.len() {
        if block[index].trim() == "required:" {
            index += 1;

            while index < block.len() {
                let trimmed = block[index].trim();
                if !trimmed.starts_with("- ") {
                    break;
                }

                required.insert(trimmed.trim_start_matches("- ").to_owned());
                index += 1;
            }
        } else {
            index += 1;
        }
    }

    required
}

fn parse_property_kind(block: &[&str]) -> Result<PropertyKind, Box<dyn Error>> {
    let mut scalar_type = None;
    let mut variants = Vec::new();
    let mut reference = None;
    let mut items = None;
    let mut index = 0;

    while index < block.len() {
        let trimmed = block[index].trim();

        if let Some(rest) = trimmed.strip_prefix("$ref:") {
            reference = Some(parse_reference(rest)?);
            index += 1;
            continue;
        }

        if let Some(rest) = trimmed.strip_prefix("type: ") {
            scalar_type = Some(rest.trim_matches('\'').to_owned());
            index += 1;
            continue;
        }

        if trimmed == "type:" {
            index += 1;

            while index < block.len() {
                let trimmed = block[index].trim();
                if !trimmed.starts_with("- ") {
                    break;
                }

                variants.push(
                    trimmed
                        .trim_start_matches("- ")
                        .trim_matches('\'')
                        .to_owned(),
                );
                index += 1;
            }

            continue;
        }

        if trimmed == "items:" {
            let items_end = next_nested_block_end(block, index + 1, leading_spaces(block[index]));
            items = Some(parse_property_kind(&block[index + 1..items_end])?);
            index = items_end;
            continue;
        }

        index += 1;
    }

    if let Some(reference) = reference {
        return Ok(PropertyKind::Ref(reference));
    }

    if !variants.is_empty() {
        return parse_variant_kind(&variants);
    }

    if let Some(scalar_type) = scalar_type {
        if scalar_type == "array" {
            let items = items.ok_or_else(|| {
                CodegenError("array schema is missing `items` declaration".to_owned())
            })?;

            return Ok(PropertyKind::Array(Box::new(items)));
        }

        return scalar_kind(&scalar_type);
    }

    Err(Box::new(CodegenError(
        "unable to parse property kind from OpenAPI schema".to_owned(),
    )))
}

fn parse_variant_kind(variants: &[String]) -> Result<PropertyKind, Box<dyn Error>> {
    let mut non_null = variants
        .iter()
        .filter(|variant| variant.as_str() != "null")
        .cloned()
        .collect::<Vec<_>>();

    if non_null.len() == 1 && variants.iter().any(|variant| variant == "null") {
        return scalar_kind(non_null.remove(0).as_str()).map(PropertyKind::optional);
    }

    Err(Box::new(CodegenError(format!(
        "unsupported OpenAPI union type: {variants:?}"
    ))))
}

fn scalar_kind(kind: &str) -> Result<PropertyKind, Box<dyn Error>> {
    match kind {
        "string" => Ok(PropertyKind::String),
        "integer" => Ok(PropertyKind::Integer),
        "number" => Ok(PropertyKind::Number),
        "boolean" => Ok(PropertyKind::Boolean),
        other => Err(Box::new(CodegenError(format!(
            "unsupported scalar type `{other}` in OpenAPI schema"
        )))),
    }
}

fn parse_reference(value: &str) -> Result<String, Box<dyn Error>> {
    value
        .trim()
        .trim_matches('\'')
        .rsplit('/')
        .next()
        .map(str::to_owned)
        .ok_or_else(|| {
            Box::new(CodegenError(format!("invalid OpenAPI reference `{value}`"))) as Box<dyn Error>
        })
}

fn parse_endpoints(schema: &str) -> Result<Vec<Endpoint>, Box<dyn Error>> {
    let lines = schema.lines().collect::<Vec<_>>();
    let mut endpoints = Vec::new();

    for &(const_name, path) in AUTH_ENDPOINTS {
        endpoints.push(parse_endpoint(&lines, const_name, path)?);
    }

    Ok(endpoints)
}

fn parse_endpoint(
    lines: &[&str],
    const_name: &str,
    path: &str,
) -> Result<Endpoint, Box<dyn Error>> {
    let start = lines
        .iter()
        .position(|line| *line == format!("  {path}:"))
        .ok_or_else(|| CodegenError(format!("endpoint `{path}` is missing from OpenAPI")))?;
    let end = next_block_end(lines, start + 1, 2);
    let block = &lines[start + 1..end];
    let mut method = None;

    for line in block {
        let trimmed = line.trim();
        let indent = leading_spaces(line);

        if indent == 4 && trimmed.ends_with(':') {
            method = Some(trimmed.trim_end_matches(':').to_ascii_uppercase());
            break;
        }
    }

    Ok(Endpoint {
        const_name: const_name.to_owned(),
        method: method.unwrap_or_else(|| "POST".to_owned()),
        path: path.to_owned(),
    })
}

fn generate_code(
    schemas: &BTreeMap<String, Schema>,
    endpoints: &[Endpoint],
) -> Result<String, Box<dyn Error>> {
    let mut output = String::new();

    output.push_str("// @generated by backend-api/build.rs. Do not edit by hand.\n");
    output.push_str("use serde::{Deserialize, Serialize};\n\n");

    for &schema_name in AUTH_SCHEMAS {
        let schema = schemas.get(schema_name).ok_or_else(|| {
            Box::new(CodegenError(format!(
                "missing parsed schema `{schema_name}`"
            ))) as Box<dyn Error>
        })?;

        output.push_str("#[derive(Clone, Debug, Deserialize, Serialize, PartialEq, Eq)]\n");
        output.push_str(&format!("pub struct {} {{\n", schema.name));

        for property in &schema.properties {
            let rust_name = to_snake_case(&property.name);
            if rust_name != property.name {
                output.push_str(&format!("    #[serde(rename = \"{}\")]\n", property.name));
            }
            output.push_str(&format!(
                "    pub {}: {},\n",
                rust_name,
                property.kind.rust_type()
            ));
        }

        output.push_str("}\n\n");
    }

    output.push_str("pub mod auth {\n");
    for endpoint in endpoints {
        output.push_str(&format!(
            "    pub const {}_METHOD: &str = \"{}\";\n",
            endpoint.const_name, endpoint.method
        ));
        output.push_str(&format!(
            "    pub const {}_PATH: &str = \"{}\";\n",
            endpoint.const_name, endpoint.path
        ));
    }
    output.push_str("}\n");

    Ok(output)
}

fn next_block_end(lines: &[&str], start: usize, indent: usize) -> usize {
    for (offset, line) in lines[start..].iter().enumerate() {
        let trimmed = line.trim();
        if !trimmed.is_empty() && leading_spaces(line) == indent && trimmed.ends_with(':') {
            return start + offset;
        }
    }

    lines.len()
}

fn next_nested_block_end(lines: &[&str], start: usize, parent_indent: usize) -> usize {
    for (offset, line) in lines[start..].iter().enumerate() {
        let trimmed = line.trim();
        if !trimmed.is_empty() && leading_spaces(line) <= parent_indent {
            return start + offset;
        }
    }

    lines.len()
}

fn leading_spaces(line: &str) -> usize {
    line.chars()
        .take_while(|character| *character == ' ')
        .count()
}

fn to_snake_case(name: &str) -> String {
    let mut output = String::new();

    for (index, character) in name.chars().enumerate() {
        if character.is_uppercase() {
            if index > 0 {
                output.push('_');
            }
            output.extend(character.to_lowercase());
        } else {
            output.push(character);
        }
    }

    output
}
