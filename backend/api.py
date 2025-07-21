#!/usr/bin/env python3

import subprocess
import sys
import json
import textwrap
import argparse

DEFAULT_GROUP_UID = "00000000-0000-0000-0000-b00000000001"
DEFAULT_PASSWORD = "abc123"

HELP_TEXT = """
    Commands:

    group                                  Get a group
        --group-uid GROUP_UID              Optional: Group UID (default: 00000000-0000-0000-0000-b00000000001)
        --password PASSWORD                Optional: Group password (default: abc123)
    
    post-group                             Post a new group
        --password PASSWORD                Required: Group password
        --title TITLE                      Required: Group title
        --members MEMBER1 MEMBER2 ...      Optional: List of member names

    post-expense                           Post a new expense
        --password PASSWORD                Required: Group password
        --title TITLE                      Required: Expense title
        --amount AMOUNT                    Required: Expense amount
        --paid-by UID1 UID2 ...            Required: List of user UIDs who paid
        
    Example: api.py post-group --password "abc123" --title "Trip to Paris" --members Alice Bob Charlie
    """

class ApiClient:
    def __init__(self, base_url="http://localhost:8080"):
        self.base_url = base_url

    def _run_httpie(self, *args):
        result = subprocess.run(args, capture_output=True, text=True)

        if result.stderr:
            print("Error during request:", result.stderr)
            sys.exit(1)

        return result.stdout

    def get_group(self, group_uid=None, password=None):
        url = f"{self.base_url}/group?ids={group_uid}&passwords={password}"
        return self._run_httpie("http", "GET", url)
    
    def post_group(self, password, title, members=None):
        """
        Post a new group to the API
        
        Args:
            password (str): Group password
            title (str): Group title
            members (list, optional): List of member names
        """
        url = f"{self.base_url}/group"
        
        # Build the request payload
        payload = {
            "password": password,
            "title": title
        }
           
        if members:
            payload["members"] = [{"name": member} for member in members]
            
        body = json.dumps(payload)
        return self._run_httpie("http", "POST", url, "Content-Type:application/json", "--raw", body)
    
    def post_expense(self, group_uid, password, title, amount, paid_by):
        """
        Post a new expense to a group
        
        Args:
            group_uid (str): Group UID where the expense will be added
            password (str): Group password
            title (str): Expense title
            amount (float): Expense amount
            paid_by (list): User UID who paid for the expense
        """
        url = f"{self.base_url}/expense/{group_uid}?password={password}"
        
        # Build the request payload
        payload = {
            "title": title,
            "amount": amount,
            "paidBy": [{"uid": uid} for uid in paid_by],
            "isSplitBetweenAll": True
        }
        
        body = json.dumps(payload)
        return self._run_httpie("http", "POST", url, "Content-Type:application/json", "--raw", body)

def main():
    if len(sys.argv) < 2:
        print(textwrap.dedent(HELP_TEXT))
        sys.exit(1)

    command = sys.argv[1]
    api = ApiClient()

    match command:
        case "group":
            parser = argparse.ArgumentParser(description="Get a group")
            parser.add_argument("--group-uid", required=False, default=DEFAULT_GROUP_UID, help="Group UID")
            parser.add_argument("--password", required=False, default=DEFAULT_PASSWORD, help="Group password")
            
            args = parser.parse_args(sys.argv[2:])

            result = api.get_group(
                group_uid=args.group_uid,
                password=args.password
            )
            print(result)
        
        case "post-group":
            parser = argparse.ArgumentParser(description="Create a new group")
            parser.add_argument("--password", required=True, help="Group password")
            parser.add_argument("--title", required=True, help="Group title")
            parser.add_argument("--members", nargs="+", help="List of member names")
            
            args = parser.parse_args(sys.argv[2:])
            
            result = api.post_group(
                password=args.password,
                title=args.title,
                members=args.members
            )
            print(result)
        
        case "post-expense":
            parser = argparse.ArgumentParser(description="Create a new expense")
            parser.add_argument("--group-uid", required=True, help="Group UID")
            parser.add_argument("--password", required=True, help="Group password")
            parser.add_argument("--title", required=True, help="Expense title")
            parser.add_argument("--amount", type=float, required=True, help="Expense amount")
            parser.add_argument("--paid-by", nargs="+", required=True, help="List of user UIDs who paid")
            
            args = parser.parse_args(sys.argv[2:])
            
            result = api.post_expense(
                group_uid=args.group_uid,
                password=args.password,
                title=args.title,
                amount=args.amount,
                paid_by=args.paid_by
            )
            print(result)
        
        case _:
            print(textwrap.dedent(HELP_TEXT))
            sys.exit(1)

if __name__ == "__main__":
    main()
