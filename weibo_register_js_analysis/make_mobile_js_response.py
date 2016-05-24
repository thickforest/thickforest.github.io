#!/usr/bin/python

header = open('mobile.js.header', 'rb').read()
data = open('mobile.js', 'rb').read()

open('mobile.js.response', 'wb').write(header % len(data) + data)
