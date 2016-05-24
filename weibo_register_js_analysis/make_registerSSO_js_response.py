#!/usr/bin/python

header = open('registerSSO.js.header', 'rb').read()
data = open('registerSSO.js', 'rb').read()

open('registerSSO.js.response', 'wb').write(header % len(data) + data)
