#!/usr/bin/env python
#coding:utf-8
import sys, os
import scapy
from scapy.all import *

def Flag(i):
	'''
	将数字类型的flag转化为有意义的字符
	'''
	i = int(i)
	l = []
	if 1 & i: l.append('F')
	if 1<<1 & i: l.append('S')
	if 1<<2 & i: l.append('R')
	if 1<<3 & i: l.append('P')
	if 1<<4 & i: l.append('A')
	if 1<<5 & i: l.append('U')
	return ''.join(l)
	

def packet_callback(bag):
	global request_packet_dic, response_packet_dic, ip_ids
	# 总是连续收到重复包，不知道为什么
	# TODO 进入包ID和流出包ID有可能重复
	ip_id = bag[IP].id
	sip = bag[IP].src
	dip = bag[IP].dst
	sport = bag[TCP].sport
	dport = bag[TCP].dport
	seq = bag[TCP].seq
	ack = bag[TCP].ack
	flag = Flag(bag[TCP].flags)
	ip_len = bag[IP].len
	ip_head_len = int(bag[IP].ihl * 4)
	tcp_head_len = int(bag[TCP].dataofs * 4)
	load_len = ip_len - ip_head_len -tcp_head_len
	load = bag[TCP].payload
	if type(load) == scapy.packet.NoPayload:
		load = ''
	else:
		load = load.load

	bag.show()

	ip = IP()
	ip.id = bag.id + 1
	ip.ttl = 64
	ip.flags = 'DF'
	ip.proto = 'tcp'
	ip.src = bag[IP].src
	ip.dst = bag[IP].dst
	tcp = TCP()
	tcp.sport = bag[TCP].sport
	tcp.dport = bag[TCP].dport
	tcp.seq = bag[TCP].seq + 1
	tcp.ack = bag[TCP].ack
	tcp.flags = 'RA'
	rst_bag = ip/tcp
	rst_bag.show()
	send(rst_bag, iface="lo")

# 抓包
# sniff(filter="tcp and port 8774", count=30, prn=lambda x:x.sprintf("{IP:%IP.src% -> %IP.dst%\n}{Raw:%Raw.load%\n}"))
sniff(iface='lo', filter="tcp and port 800", count=1, prn=packet_callback)

