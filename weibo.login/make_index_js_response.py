#!/usr/bin/python

header = open('index.js.header', 'rb').read()
data = open('index.js', 'rb').read()

open('index.js.response', 'wb').write(header % len(data) + data)
