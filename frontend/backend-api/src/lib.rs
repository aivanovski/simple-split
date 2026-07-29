mod client;

pub const DEFAULT_BASE_URL: &str = "http://127.0.0.1:8080";

include!(concat!(env!("OUT_DIR"), "/generated.rs"));

pub use client::{ApiClient, ApiError};
