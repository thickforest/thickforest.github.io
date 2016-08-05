#!/usr/bin/python
#coding:utf-8
import os, sys
import json, re
import requests
import time
import base64
import random

def debug():
	import pdb
	pdb.set_trace()

def current_time13():
	import time
	import random
	return "%d%d" % (int(time.time()), random.randint(100, 999))

def current_time10():
	import time
	return "%d" % (int(time.time()))

def encrypt_passwd(servertime, nonce, rsaPubkey, passwd):
	#print servertime, nonce, rsaPubkey, passwd
	login_passwd_encrypt_js = open('login_passwd_encrypt.js', 'rb').read().replace('AAAAA', servertime).replace('BBBBB', nonce).replace('CCCCC', rsaPubkey).replace('DDDDD', passwd)
	open('login_passwd_encrypt.tmp', 'wb').write(login_passwd_encrypt_js)
	encrypt_passwd = os.popen('js login_passwd_encrypt.tmp').read().strip()
	#print encrypt_passwd
	os.system('rm -rf login_passwd_encrypt.tmp')
	return encrypt_passwd

def get_headers():
	headers = {}
	headers['User-Agent'] = 'Mozilla/5.0 (X11; Linux x86_64; rv:10.0.12) Gecko/20130104 Firefox/10.0.12'
	headers['Accept'] = 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8'
	headers['Accept-Language'] = 'en-us,en;q=0.5'
	headers['Accept-Encoding'] = 'gzip, deflate'
	headers['Content-Type'] = 'application/x-www-form-urlencoded'
	headers['Connection'] = 'keep-alive'
	return headers

def get_nickname(sess, nickname, referer_url):
	headers = get_headers()
	headers['X-Requested-With'] = 'XMLHttpRequest'
	headers['Referer'] = referer_url
	url = 'http://weibo.com/nguide/aj/checknickname?type=nickname&value=%s&__rnd=%s' % (nickname, current_time13())
	recv_data = sess.get(url, headers=headers).text
	json_data = json.loads(recv_data)
	if json_data[u'code'] == '100000':
		return nickname
	else:
		other_nicknames = json_data[u'data'][u'iodata']
		return other_nicknames[random.randint(0, len(other_nicknames)-1)]

def login(mobile, passwd, nickname):
	sess = requests.session()
	home_url = 'http://weibo.com/login.php'
	
	# 执行prelogin请求
	headers = get_headers()
	headers['Referer'] = home_url
	del headers['Content-Type']
	prelogin_url = 'http://login.sina.com.cn/sso/prelogin.php?entry=weibo&callback=sinaSSOController.preloginCallBack&su=%s&rsakt=mod&checkpin=1&client=ssologin.js(v1.4.18)&_=%s' % (base64.b64encode(mobile), current_time13())
	recv_data = sess.get(prelogin_url, headers = headers).text
	json_data = recv_data[recv_data.find('{'):recv_data.find('}') + 1]
	pre_data = json.loads(json_data)
	#print json_data
	
	# 执行login请求
	headers = get_headers()
	headers['Referer'] = home_url
	t = current_time10()
	#headers['Cookie'] = 'SINAGLOBAL=60.208.111.198_%s.63780; Apache=60.208.111.198_%s.63785' % (t, t)
	login_url = 'http://login.sina.com.cn/sso/login.php?client=ssologin.js(v1.4.18)'
	post_data = {}
	post_data['entry'] = 'weibo'
	post_data['gateway'] = '1'
	post_data['from'] = ''
	post_data['savestate'] = '7'
	post_data['useticket'] = '1'
	post_data['pagerefer'] = 'http://login.sina.com.cn/sso/logout.php?entry=miniblog&r=http%3A%2F%2Fweibo.com%2Flogout.php%3Fbackurl%3D%252F'
	post_data['vsnf'] = '1'
	post_data['su'] = base64.b64encode(mobile)
	post_data['service'] = 'miniblog'
	#servertime = current_time10()
	servertime = str(pre_data[u'servertime'] + 22)	# 在服务器时间上加个随机数
	post_data['servertime'] = servertime
	nonce = pre_data[u'nonce']
	post_data['nonce'] = nonce
	post_data['pwencode'] = 'rsa2'
	post_data['rsakv'] = pre_data[u'rsakv']
	post_data['sp'] = encrypt_passwd(servertime, nonce, pre_data[u'pubkey'], passwd)
	#post_data['sr'] = '1366*768'
	post_data['encoding'] = 'UTF-8'
	post_data['prelt'] = '2765'	# 随机数
	post_data['url'] = 'http://weibo.com/ajaxlogin.php?framelogin=1&callback=parent.sinaSSOController.feedBackUrlCallBack'
	post_data['returntype'] = 'META'
	recv_data = sess.post(login_url, post_data, headers = headers).text
	url = re.findall(r"(http://passport.weibo.com/[^']*)'", recv_data)[0]
	
	# 之前都是login.sina.com.cn的cookie，只有执行下面的请求才能得到.weibo.com的cookie
	headers['Referer'] = 'http://login.sina.com.cn/sso/login.php?client=ssologin.js(v1.4.18)'
	del headers['Content-Type']
	recv_data = sess.get(url, headers = headers).text

	response = sess.get(home_url, headers = headers)
	if response.url.find('nguide') != -1:
		# 注册后第一次登录
		new_nickname = get_nickname(sess, nickname, response.url)
		headers = get_headers()
		headers['X-Requested-With'] = 'XMLHttpRequest'
		url = 'http://weibo.com/nguide/aj/register4?__rnd=%s' % current_time13()
		post_data = {}
		post_data['nickname'] = new_nickname
		post_data['birthday'] = '%d-%d-%d' % (random.randint(1980, 2000), random.randint(1, 12), random.randint(1, 28))
		if random.randint(0, 1):
			post_data['gender'] = 'm'
		else:
			post_data['gender'] = 'f'
		post_data['province'] = random.randint(1, 32)
		post_data['city'] = 1
		post_data['_t'] = 0
		headers['Referer'] = response.url
		next_response = sess.post(url, post_data, headers=headers)
		print 'interests:', next_response.url

		interests_json = {}
		post_data = {'data':json.dumps(interests_json)}
		url = 'http://weibo.com/nguide/aj/finish4'
		headers['Referer'] = next_response.url
		last_response = sess.post(url, post_data, headers = headers)
		print last_response.url
		return new_nickname
	else:
		print response.url
		return ''
		

if __name__ == '__main__':
	#nickname = login('14792363508', 'qwer1234', "侍剑")
	#nickname = login('17194443292', 'qwer1234', "悲剧")
	nickname = login('18500446146', 'qwer1234', "柳叶刀")
	print nickname
