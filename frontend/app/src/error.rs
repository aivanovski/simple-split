use std::fmt::{Display, Formatter};

#[derive(Debug)]
pub enum AppError {
    GenericError(String),
    Api(ApiError),
}

#[derive(Debug)]
pub enum ApiError {
    Network(String),
}

impl Display for ApiError {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        let message = match self {
            ApiError::Network(message) => format!("ApiError::Network({message})"),
        };

        f.write_str(message.as_str())
    }
}
