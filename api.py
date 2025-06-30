#!/usr/bin/env python3

import subprocess
import sys
import json
import textwrap

DEFAULT_USER = "mickey@disney.com"

HELP_TEXT = """
    Commands:

    login                                  Sends login request with default credentials
    user                                   Get all get_users
    group                                  Get all groups
    """

class ApiClient:
    def __init__(self, base_url="http://localhost:8080"):
        self.base_url = base_url

    def _run_httpie(self, *args):
        """Run an HTTPie command and return parsed JSON, or raw output on error."""
        result = subprocess.run(args, capture_output=True, text=True)

        if result.stderr:
            print("Error during request:", result.stderr)
            sys.exit(1)

        return result.stdout

    def login(self, email, password):
        url = f"{self.base_url}/login"
        body = json.dumps({"email": email, "password": password})
        return self._run_httpie("http", "POST", url, "Content-Type:application/json", "--raw", body)

    def get_auth_header(self):
        resposne = self.login(email=DEFAULT_USER, password="abc123")
        data = json.loads(resposne)
        token = data["token"]
        return f"Authorization:Bearer {token}"

    def get_users(self):
        return self._run_httpie("http", "GET", f"{self.base_url}/user", self.get_auth_header())

    def get_groups(self):
        return self._run_httpie("http", "GET", f"{self.base_url}/group", self.get_auth_header())

def main():
    if len(sys.argv) < 2:
        print(textwrap.dedent(HELP_TEXT))
        sys.exit(1)

    command = sys.argv[1]
    api = ApiClient()

    match command:
        case "login":
            print(api.login(email=DEFAULT_USER, password="abc123"))
        case "user":
            print(api.get_users())
        case "group":
            print(api.get_groups())
        case _:
            print(textwrap.dedent(HELP_TEXT))
            sys.exit(1)

if __name__ == "__main__":
    main()
