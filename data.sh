#!/bin/sh

token=$(http post localhost:8080/login email=mickey@mail.com password=abc123 | grep token | cut -d'"' -f4)

http -A bearer -a $token post localhost:8080/expense/00000000-0000-00ff-0000-000000000001 <<EOF
{
    "title": "Dinner",
    "amount": 20.82,
    "paidBy": [
      { "uid": "00000000-0000-0002-0000-000000000003" }
    ],
    "splitBetween": [
      { "uid":"00000000-0000-0002-0000-000000000001" },
      { "uid":"00000000-0000-0002-0000-000000000002" },
      { "uid":"00000000-0000-0002-0000-000000000003" }
    ]
}
EOF

http -A bearer -a $token post localhost:8080/expense/00000000-0000-00ff-0000-000000000001 <<EOF
{
    "title": "Hotel",
    "amount": 250,
    "paidBy": [
      { "uid": "00000000-0000-0002-0000-000000000002" }
    ],
    "splitBetween": [
      { "uid":"00000000-0000-0002-0000-000000000001" },
      { "uid":"00000000-0000-0002-0000-000000000002" },
      { "uid":"00000000-0000-0002-0000-000000000003" }
    ]
}
EOF

http -A bearer -a $token post localhost:8080/expense/00000000-0000-00ff-0000-000000000001 <<EOF
{
    "title": "Coffee to go",
    "amount": 12,
    "paidBy": [
      { "uid": "00000000-0000-0002-0000-000000000003" }
    ],
    "splitBetween": [
      { "uid":"00000000-0000-0002-0000-000000000001" },
      { "uid":"00000000-0000-0002-0000-000000000003" }
    ]
}
EOF