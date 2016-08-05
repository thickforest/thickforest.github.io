public static void login(String u, String p) {
 
		DefaultHttpClient client = new DefaultHttpClient();
 
		try {
			/**获得rsaPubkey,rsakv,servertime等参数值，此获取参数值的方法的形式，要感谢网上一大哥发的帖子**/
			HashMap&lt;String, String&gt; params = preLogin(encodeAccount(u),client);
 
			/********登录操作*********/
			HttpPost post = new HttpPost(
					"http://login.sina.com.cn/sso/login.php?client=ssologin.js(v1.4.5)");
			post
					.setHeader("Accept",
							"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			post
					.setHeader("User-Agent",
							"Mozilla/5.0 (Windows NT 5.1; rv:9.0.1) Gecko/20100101 Firefox/9.0.1");
 
			post.setHeader("Accept-Language", "zh-cn,zh;q=0.5");
			post.setHeader("Accept-Charset", "GB2312,utf-8;q=0.7,*;q=0.7");
			post.setHeader("Referer",
					"http://weibo.com/?c=spr_web_sq_firefox_weibo_t001");
			post.setHeader("Content-Type", "application/x-www-form-urlencoded");
 
			String nonce = makeNonce(6);
 
			List nvps = new ArrayList();
			nvps.add(new BasicNameValuePair("encoding", "UTF-8"));
			nvps.add(new BasicNameValuePair("entry", "weibo"));
			nvps.add(new BasicNameValuePair("from", ""));
			nvps.add(new BasicNameValuePair("gateway", "1"));
			nvps.add(new BasicNameValuePair("nonce", nonce));
			nvps.add(new BasicNameValuePair("pagerefer", "http://i.firefoxchina.cn/old/"));
			nvps.add(new BasicNameValuePair("prelt", "111"));
			nvps.add(new BasicNameValuePair("pwencode", "rsa2"));
			nvps.add(new BasicNameValuePair("returntype", "META"));
			nvps.add(new BasicNameValuePair("rsakv", params.get("rsakv")));
			nvps.add(new BasicNameValuePair("savestate", "0"));
			nvps.add(new BasicNameValuePair("servertime", params.get("servertime")));
 
			nvps.add(new BasicNameValuePair("service", "miniblog"));
 
			/******************** *加密密码 ***************************/
			ScriptEngineManager sem = new ScriptEngineManager();
			ScriptEngine se = sem.getEngineByName("javascript");
 
		    se.eval(getJs());
		    String pass = "";
 
		    if (se instanceof Invocable) {
				Invocable invoke = (Invocable) se;
				// 调用preprocess方法，并传入两个参数密码和验证码
 
				pass = invoke.invokeFunction("getpass",
						p, params.get("servertime"), nonce,params.get("pubkey")).toString();
 
				System.out.println("c = " + pass);
			}
 
			nvps.add(new BasicNameValuePair("sp",pass));
			nvps.add(new BasicNameValuePair("su", encodeAccount(u)));
			nvps
			.add(new BasicNameValuePair(
					"url",
					"http://weibo.com/ajaxlogin.php?framelogin=1&amp;callback=parent.sinaSSOController.feedBackUrlCallBack"));
 
			nvps.add(new BasicNameValuePair("useticket", "1"));
 
			nvps.add(new BasicNameValuePair("vsnf", "1"));
 
			post.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
 
			HttpResponse response = client.execute(post);
 
			String entity = EntityUtils.toString(response.getEntity());
 
			if (entity.replace("\"", "").indexOf("retcode=0") &gt; -1) {
				String url = entity.substring(entity
						.indexOf("http://weibo.com/sso/login.php?"), entity
						.indexOf("code=0")+6 );
 
				String strScr = "";      
				String nick = "暂无";     //昵称
 
				// 获取到实际url进行连接
				HttpGet getMethod = new HttpGet(url);
				response = client.execute(getMethod);
				entity = EntityUtils.toString(response.getEntity());
 
				nick = entity.substring(entity.indexOf("displayname") + 14,
						entity.lastIndexOf("userdomain") - 3).trim();
 
				url = entity.substring(entity.indexOf("userdomain") + 13,
						entity.lastIndexOf("\""));
				getMethod = new HttpGet("http://weibo.com/"+url);
				response = client.execute(getMethod);
				entity = EntityUtils.toString(response.getEntity());
 
				System.out.println(entity);
 
			}
 
		} catch (Exception e) {
			e.printStackTrace();
 
		}
 
	}
 
	/** 
     * 根据URL,get网页 
     *  
     * @param url 
     * @throws IOException 
     */  
    private static String get(String url,DefaultHttpClient client) throws IOException {  
        HttpGet get = new HttpGet(url);  
        HttpResponse response = client.execute(get);  
        System.out.println(response.getStatusLine());  
        HttpEntity entity = response.getEntity();  
        String result = dump(entity);  
        get.abort();  
        return result;  
    }  
 
    /** 
     * 新浪微博预登录，获取密码加密公钥 
     *  
     * @param unameBase64 
     * @return 返回从结果获取的参数的哈希表 
     * @throws IOException 
     */  
    private static HashMap&lt;String, String&gt; preLogin(String unameBase64,DefaultHttpClient client)  
            throws IOException {  
        String url = "http://login.sina.com.cn/sso/prelogin.php?entry=weibo&amp;callback=sinaSSOController.preloginCallBack&amp;su=&amp;rsakt=mod&amp;client=ssologin.js(v1.4.5)&amp;_=" + "_=" + new Date().getTime();  
        return getParaFromResult(get(url,client));  
    }  
 
    /** 
     * 从新浪返回的结果字符串中获得参数 
     *  
     * @param result 
     * @return 
     */  
    private static HashMap&lt;String, String&gt; getParaFromResult(String result) {  
        HashMap&lt;String, String&gt; hm = new HashMap&lt;String, String&gt;();  
        result = result.substring(result.indexOf("{") + 1, result.indexOf("}"));  
        String[] r = result.split(",");  
        String[] temp;  
        for (int i = 0; i &lt; r.length; i++) {  
            temp = r[i].split(":");  
            for (int j = 0; j &lt; 2; j++) {                   if (temp[j].contains("\""))                       temp[j] = temp[j].substring(1, temp[j].length() - 1);               }               hm.put(temp[0], temp[1]);           }           return hm;       }            /**        * 打印页面        *         * @param entity        * @throws IOException        */       private static String dump(HttpEntity entity) throws IOException {           BufferedReader br = new BufferedReader(new InputStreamReader(                   entity.getContent(), "utf8"));           return IOUtils.toString(br);       }   	 	//rsa2加密 	public static String getJs() { 		String js = 		"var sinaSSOEncoder=sinaSSOEncoder||{};(function(){var hexcase=0;var chrsz=8;this.hex_sha1=function(s){return binb2hex(core_sha1(str2binb(s),s.length*chrsz));};var core_sha1=function(x,len){x[len&gt;&gt;5]|=0x80&lt;&lt;(24-len%32);x[((len+64&gt;&gt;9)&lt;&lt;4)+15]=len;var w=Array(80);var a=1732584193;var b=-271733879;var c=-1732584194;var d=271733878;var e=-1009589776;for(var i=0;i&lt;x.length;i+=16){var olda=a;var oldb=b;var oldc=c;var oldd=d;var olde=e;for(var j=0;j&lt;80;j++){if(j&lt;16)w[j]=x[i+j];else w[j]=rol(w[j-3]^w[j-8]^w[j-14]^w[j-16],1);var t=safe_add(safe_add(rol(a,5),sha1_ft(j,b,c,d)),safe_add(safe_add(e,w[j]),sha1_kt(j)));e=d;d=c;c=rol(b,30);b=a;a=t;}a=safe_add(a,olda);b=safe_add(b,oldb);c=safe_add(c,oldc);d=safe_add(d,oldd);e=safe_add(e,olde);}return Array(a,b,c,d,e);};var sha1_ft=function(t,b,c,d){if(t&lt;20)return(b&amp;c)|((~b)&amp;d);if(t&lt;40)return b^c^d;if(t&lt;60)return(b&amp;c)|(b&amp;d)|(c&amp;d);return b^c^d;};var sha1_kt=function(t){return(t&lt;20)?1518500249:(t&lt;40)?1859775393:(t&lt;60)?-1894007588:-899497514;};var safe_add=function(x,y){var lsw=(x&amp;0xFFFF)+(y&amp;0xFFFF);var msw=(x&gt;&gt;16)+(y&gt;&gt;16)+(lsw&gt;&gt;16);return(msw&lt;&lt;16)|(lsw&amp;0xFFFF);};var rol=function(num,cnt){return(num&lt;&lt;cnt)|(num&gt;&gt;&gt;(32-cnt));};var str2binb=function(str){var bin=Array();var mask=(1&lt;&lt;chrsz)-1;for(var i=0;i&lt;str.length*chrsz;i+=chrsz)bin[i&gt;&gt;5]|=(str.charCodeAt(i/chrsz)&amp;mask)&lt;&lt;(24-i%32);return bin;};var binb2hex=function(binarray){var hex_tab=hexcase?'0123456789ABCDEF':'0123456789abcdef';var str='';for(var i=0;i&lt;binarray.length*4;i++){str+=hex_tab.charAt((binarray[i&gt;&gt;2]&gt;&gt;((3-i%4)*8+4))&amp;0xF)+hex_tab.charAt((binarray[i&gt;&gt;2]&gt;&gt;((3-i%4)*8))&amp;0xF);}return str;};this.base64={encode:function(input){input=''+input;if(input=='')return '';var output='';var chr1,chr2,chr3='';var enc1,enc2,enc3,enc4='';var i=0;do{chr1=input.charCodeAt(i++);chr2=input.charCodeAt(i++);chr3=input.charCodeAt(i++);enc1=chr1&gt;&gt;2;enc2=((chr1&amp;3)&lt;&lt;4)|(chr2&gt;&gt;4);enc3=((chr2&amp;15)&lt;&lt;2)|(chr3&gt;&gt;6);enc4=chr3&amp;63;if(isNaN(chr2)){enc3=enc4=64;}else if(isNaN(chr3)){enc4=64;}output=output+this._keys.charAt(enc1)+this._keys.charAt(enc2)+this._keys.charAt(enc3)+this._keys.charAt(enc4);chr1=chr2=chr3='';enc1=enc2=enc3=enc4='';}while(i&lt;input.length);return output;},_keys:'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/='};}).call(sinaSSOEncoder);;(function(){var dbits;var canary=0xdeadbeefcafe;var j_lm=((canary&amp;0xffffff)==0xefcafe);function BigInteger(a,b,c){if(a!=null)if('number'==typeof a)this.fromNumber(a,b,c);else if(b==null &amp;&amp; 'string' !=typeof a)this.fromString(a,256);else this.fromString(a,b);}function nbi(){return new BigInteger(null);}function am1(i,x,w,j,c,n){while(--n&gt;=0){var v=x*this[i++]+w[j]+c;c=Math.floor(v/0x4000000);w[j++]=v&amp;0x3ffffff;}return c;}function am2(i,x,w,j,c,n){var xl=x&amp;0x7fff,xh=x&gt;&gt;15;while(--n&gt;=0){var l=this[i]&amp;0x7fff;var h=this[i++]&gt;&gt;15;var m=xh*l+h*xl;l=xl*l+((m&amp;0x7fff)&lt;&lt;15)+w[j]+(c&amp;0x3fffffff);c=(l&gt;&gt;&gt;30)+(m&gt;&gt;&gt;15)+xh*h+(c&gt;&gt;&gt;30);w[j++]=l&amp;0x3fffffff;}return c;}function am3(i,x,w,j,c,n){var xl=x&amp;0x3fff,xh=x&gt;&gt;14;while(--n&gt;=0){var l=this[i]&amp;0x3fff;var h=this[i++]&gt;&gt;14;var m=xh*l+h*xl;l=xl*l+((m&amp;0x3fff)&lt;&lt;14)+w[j]+c;c=(l&gt;&gt;28)+(m&gt;&gt;14)+xh*h;w[j++]=l&amp;0xfffffff;}return c;}BigInteger.prototype.am=am3;dbits=28;BigInteger.prototype.DB=dbits;BigInteger.prototype.DM=((1&lt;&lt;dbits)-1);BigInteger.prototype.DV=(1&lt;&lt;dbits);var BI_FP=52;BigInteger.prototype.FV=Math.pow(2,BI_FP);BigInteger.prototype.F1=BI_FP-dbits;BigInteger.prototype.F2=2*dbits-BI_FP;var BI_RM='0123456789abcdefghijklmnopqrstuvwxyz';var BI_RC=new Array();var rr,vv;rr='0'.charCodeAt(0);for(vv=0;vv&lt;=9;++vv)BI_RC[rr++]=vv;rr='a'.charCodeAt(0);for(vv=10;vv&lt;36;++vv)BI_RC[rr++]=vv;rr='A'.charCodeAt(0);for(vv=10;vv&lt;36;++vv)BI_RC[rr++]=vv;function int2char(n){return BI_RM.charAt(n);}function intAt(s,i){var c=BI_RC[s.charCodeAt(i)];return(c==null)?-1:c;}function bnpCopyTo(r){for(var i=this.t-1;i&gt;=0;--i)r[i]=this[i];r.t=this.t;r.s=this.s;}function bnpFromInt(x){this.t=1;this.s=(x&lt;0)?-1:0;if(x&gt;0)this[0]=x;else if(x&lt;-1)this[0]=x+DV;else this.t=0;}function nbv(i){var r=nbi();r.fromInt(i);return r;}function bnpFromString(s,b){var k;if(b==16)k=4;else if(b==8)k=3;else if(b==256)k=8;else if(b==2)k=1;else if(b==32)k=5;else if(b==4)k=2;else{this.fromRadix(s,b);return;}this.t=0;this.s=0;var i=s.length,mi=false,sh=0;while(--i&gt;=0){var x=(k==8)?s[i]&amp;0xff:intAt(s,i);if(x&lt;0){if(s.charAt(i)=='-')mi=true;continue;}mi=false;if(sh==0)this[this.t++]=x;else if(sh+k&gt;this.DB){this[this.t-1]|=(x&amp;((1&lt;&lt;(this.DB-sh))-1))&lt;&lt;sh;this[this.t++]=(x&gt;&gt;(this.DB-sh));}else "
				+ " this[this.t-1]|=x&lt;&lt;sh;sh+=k;if(sh&gt;=this.DB)sh-=this.DB;}if(k==8&amp;&amp;(s[0]&amp;0x80)!=0){this.s=-1;if(sh&gt;0)this[this.t-1]|=((1&lt;&lt;(this.DB-sh))-1)&lt;&lt;sh;}this.clamp();if(mi)BigInteger.ZERO.subTo(this,this);}function bnpClamp(){var c=this.s&amp;this.DM;while(this.t&gt;0&amp;&amp;this[this.t-1]==c)--this.t;}function bnToString(b){if(this.s&lt;0)return '-'+this.negate().toString(b);var k;if(b==16)k=4;else if(b==8)k=3;else if(b==2)k=1;else if(b==32)k=5;else if(b==4)k=2;else return this.toRadix(b);var km=(1&lt;&lt;k)-1,d,m=false,r='',i=this.t;var p=this.DB-(i*this.DB)%k;if(i--&gt;0){if(p&lt;this.DB&amp;&amp;(d=this[i]&gt;&gt;p)&gt;0){m=true;r=int2char(d);}while(i&gt;=0){if(p&lt;k){d=(this[i]&amp;((1&lt;&lt;p)-1))&lt;&lt;(k-p);d|=this[--i]&gt;&gt;(p+=this.DB-k);}else{d=(this[i]&gt;&gt;(p-=k))&amp;km;if(p&lt;=0){p+=this.DB;--i;}}if(d&gt;0)m=true;if(m)r+=int2char(d);}}return m?r:'0';}function bnNegate(){var r=nbi();BigInteger.ZERO.subTo(this,r);return r;}function bnAbs(){return(this.s&lt;0)?this.negate():this;}function bnCompareTo(a){var r=this.s-a.s;if(r!=0)return r;var i=this.t;r=i-a.t;if(r!=0)return r;while(--i&gt;=0)if((r=this[i]-a[i])!=0)return r;return 0;}function nbits(x){var r=1,t;if((t=x&gt;&gt;&gt;16)!=0){x=t;r+=16;}if((t=x&gt;&gt;8)!=0){x=t;r+=8;}if((t=x&gt;&gt;4)!=0){x=t;r+=4;}if((t=x&gt;&gt;2)!=0){x=t;r+=2;}if((t=x&gt;&gt;1)!=0){x=t;r+=1;}return r;}function bnBitLength(){if(this.t&lt;=0)return 0;return this.DB*(this.t-1)+nbits(this[this.t-1]^(this.s&amp;this.DM));}function bnpDLShiftTo(n,r){var i;for(i=this.t-1;i&gt;=0;--i)r[i+n]=this[i];for(i=n-1;i&gt;=0;--i)r[i]=0;r.t=this.t+n;r.s=this.s;}function bnpDRShiftTo(n,r){for(var i=n;i&lt;this.t;++i)r[i-n]=this[i];r.t=Math.max(this.t-n,0);r.s=this.s;}function bnpLShiftTo(n,r){var bs=n%this.DB;var cbs=this.DB-bs;var bm=(1&lt;&lt;cbs)-1;var ds=Math.floor(n/this.DB),c=(this.s&lt;&lt;bs)&amp;this.DM,i;for(i=this.t-1;i&gt;=0;--i){r[i+ds+1]=(this[i]&gt;&gt;cbs)|c;c=(this[i]&amp;bm)&lt;&lt;bs;}for(i=ds-1;i&gt;=0;--i)r[i]=0;r[ds]=c;r.t=this.t+ds+1;r.s=this.s;r.clamp();}function bnpRShiftTo(n,r){r.s=this.s;var ds=Math.floor(n/this.DB);if(ds&gt;=this.t){r.t=0;return;}var bs=n%this.DB;var cbs=this.DB-bs;var bm=(1&lt;&lt;bs)-1;r[0]=this[ds]&gt;&gt;bs;for(var i=ds+1;i&lt;this.t;++i){r[i-ds-1]|=(this[i]&amp;bm)&lt;&lt;cbs;r[i-ds]=this[i]&gt;&gt;bs;}if(bs&gt;0)r[this.t-ds-1]|=(this.s&amp;bm)&lt;&lt;cbs;r.t=this.t-ds;r.clamp();}function bnpSubTo(a,r){var i=0,c=0,m=Math.min(a.t,this.t);while(i&lt;m){c+=this[i]-a[i];r[i++]=c&amp;this.DM;c&gt;&gt;=this.DB;}if(a.t&lt;this.t){c-=a.s;while(i&lt;this.t){c+=this[i];r[i++]=c&amp;this.DM;c&gt;&gt;=this.DB;}c+=this.s;}else{c+=this.s;while(i&lt;a.t){c-=a[i];r[i++]=c&amp;this.DM;c&gt;&gt;=this.DB;}c-=a.s;}r.s=(c&lt;0)?-1:0;if(c&lt;-1)r[i++]=this.DV+c;else if(c&gt;0)r[i++]=c;r.t=i;r.clamp();}function bnpMultiplyTo(a,r){var x=this.abs(),y=a.abs();var i=x.t;r.t=i+y.t;while(--i&gt;=0)r[i]=0;for(i=0;i&lt;y.t;++i)r[i+x.t]=x.am(0,y[i],r,i,0,x.t);r.s=0;r.clamp();if(this.s!=a.s)BigInteger.ZERO.subTo(r,r);}function bnpSquareTo(r){var x=this.abs();var i=r.t=2*x.t;while(--i&gt;=0)r[i]=0;for(i=0;i&lt;x.t-1;++i){var c=x.am(i,x[i],r,2*i,0,1);if((r[i+x.t]+=x.am(i+1,2*x[i],r,2*i+1,c,x.t-i-1))&gt;=x.DV){r[i+x.t]-=x.DV;r[i+x.t+1]=1;}}if(r.t&gt;0)r[r.t-1]+=x.am(i,x[i],r,2*i,0,1);r.s=0;r.clamp();}function bnpDivRemTo(m,q,r){var pm=m.abs();if(pm.t&lt;=0)return;var pt=this.abs();if(pt.t&lt;pm.t){if(q!=null)q.fromInt(0);if(r!=null)this.copyTo(r);return;}if(r==null)r=nbi();var y=nbi(),ts=this.s,ms=m.s;var nsh=this.DB-nbits(pm[pm.t-1]);if(nsh&gt;0){pm.lShiftTo(nsh,y);pt.lShiftTo(nsh,r);}else{pm.copyTo(y);pt.copyTo(r);}var ys=y.t;var y0=y[ys-1];if(y0==0)return;var yt=y0*(1&lt;&lt;this.F1)+((ys&gt;1)?y[ys-2]&gt;&gt;this.F2:0);var d1=this.FV/yt,d2=(1&lt;&lt;this.F1)/yt,e=1&lt;&lt;this.F2;var i=r.t,j=i-ys,t=(q==null)?nbi():q;y.dlShiftTo(j,t);if(r.compareTo(t)&gt;=0){r[r.t++]=1;r.subTo(t,r);}BigInteger.ONE.dlShiftTo(ys,t);t.subTo(y,y);while(y.t&lt;ys)y[y.t++]=0;while(--j&gt;=0){var qd=(r[--i]==y0)?this.DM:Math.floor(r[i]*d1+(r[i-1]+e)*d2);if((r[i]+=y.am(0,qd,r,j,0,ys))&lt;qd){y.dlShiftTo(j,t);r.subTo(t,r);while(r[i]&lt;--qd)r.subTo(t,r);}}if(q!=null){r.drShiftTo(ys,q);if(ts!=ms)BigInteger.ZERO.subTo(q,q);}r.t=ys;r.clamp();if(nsh&gt;0)r.rShiftTo(nsh,r);if(ts&lt;0)BigInteger.ZERO.subTo(r,r);}function bnMod(a){var r=nbi();this.abs().divRemTo(a,null,r);if(this.s&lt;0&amp;&amp;r.compareTo(BigInteger.ZERO)&gt;0)a.subTo(r,r);return r;}function Classic(m){this.m=m;}function cConvert(x){if(x.s&lt;0||x.compareTo(this.m)&gt;=0)return x.mod(this.m);else return x;}function cRevert(x){return x;}function cReduce(x){x.divRemTo(this.m,null,x);}function cMulTo(x,y,r){x.multiplyTo(y,r);this.reduce(r);}function cSqrTo(x,r){x.squareTo(r);this.reduce(r);}Classic.prototype.convert=cConvert;Classic.prototype.revert=cRevert;Classic.prototype.reduce=cReduce;Classic.prototype.mulTo=cMulTo;Classic.prototype.sqrTo=cSqrTo;function bnpInvDigit(){if(this.t&lt;1)return 0;var x=this[0];if((x&amp;1)==0)return 0;var y=x&amp;3;y=(y*(2-(x&amp;0xf)*y))&amp;0xf;y=(y*(2-(x&amp;0xff)*y))&amp;0xff;y=(y*(2-(((x&amp;0xffff)*y)&amp;0xffff)))&amp;0xffff;y=(y*(2-x*y%this.DV))%this.DV;return(y&gt;0)?this.DV-y:-y;}function Montgomery(m){this.m=m;this.mp=m.invDigit();this.mpl=this.mp&amp;0x7fff;this.mph=this.mp&gt;&gt;15;this.um=(1&lt;&lt;(m.DB-15))-1;this.mt2=2*m.t;}function montConvert(x){var r=nbi();x.abs().dlShiftTo(this.m.t,r);r.divRemTo(this.m,null,r);if(x.s&lt;0&amp;&amp;r.compareTo(BigInteger.ZERO)&gt;0)this.m.subTo(r,r);return r;}function montRevert(x){var r=nbi();x.copyTo(r);this.reduce(r);return r;}function montReduce(x){while(x.t&lt;=this.mt2)x[x.t++]=0;for(var i=0;i&lt;this.m.t;++i){var j=x[i]&amp;0x7fff;var u0=(j*this.mpl+(((j*this.mph+(x[i]&gt;&gt;15)*this.mpl)&amp;this.um)&lt;&lt;15))&amp;x.DM;j=i+this.m.t;x[j]+=this.m.am(0,u0,x,i,0,this.m.t);while(x[j]&gt;=x.DV){x[j]-=x.DV;x[++j]++;}}x.clamp();x.drShiftTo(this.m.t,x);if(x.compareTo(this.m)&gt;=0)x.subTo(this.m,x);}function montSqrTo(x,r){x.squareTo(r);this.reduce(r);}function montMulTo(x,y,r){x.multiplyTo(y,r);this.reduce(r);}Montgomery.prototype.convert=montConvert;Montgomery.prototype.revert=montRevert;Montgomery.prototype.reduce=montReduce;Montgomery.prototype.mulTo=montMulTo;Montgomery.prototype.sqrTo=montSqrTo;function bnpIsEven(){return((this.t&gt;0)?(this[0]&amp;1):this.s)==0;}function bnpExp(e,z){if(e&gt;0xffffffff||e&lt;1)return BigInteger.ONE;var r=nbi(),r2=nbi(),g=z.convert(this),i=nbits(e)-1;g.copyTo(r);while(--i&gt;=0){z.sqrTo(r,r2);if((e&amp;(1&lt;&lt;i))&gt;0)z.mulTo(r2,g,r);else{var t=r;r=r2;r2=t;}}return z.revert(r);}function bnModPowInt(e,m){var z;if(e&lt;256||m.isEven())z=new Classic(m);else z=new Montgomery(m);return this.exp(e,z);}BigInteger.prototype.copyTo=bnpCopyTo;BigInteger.prototype.fromInt=bnpFromInt;BigInteger.prototype.fromString=bnpFromString;BigInteger.prototype.clamp=bnpClamp;BigInteger.prototype.dlShiftTo=bnpDLShiftTo;BigInteger.prototype.drShiftTo=bnpDRShiftTo;BigInteger.prototype.lShiftTo=bnpLShiftTo;BigInteger.prototype.rShiftTo=bnpRShiftTo;BigInteger.prototype.subTo=bnpSubTo;BigInteger.prototype.multiplyTo=bnpMultiplyTo;BigInteger.prototype.squareTo=bnpSquareTo;BigInteger.prototype.divRemTo=bnpDivRemTo;BigInteger.prototype.invDigit=bnpInvDigit;BigInteger.prototype.isEven=bnpIsEven;BigInteger.prototype.exp=bnpExp;BigInteger.prototype.toString=bnToString;BigInteger.prototype.negate=bnNegate;BigInteger.prototype.abs=bnAbs;BigInteger.prototype.compareTo=bnCompareTo;BigInteger.prototype.bitLength=bnBitLength;BigInteger.prototype.mod=bnMod;BigInteger.prototype.modPowInt=bnModPowInt;BigInteger.ZERO=nbv(0);BigInteger.ONE=nbv(1);function Arcfour(){this.i=0;this.j=0;this.S=new Array();}function ARC4init(key){var i,j,t;for(i=0;i&lt;256;++i)this.S[i]=i;j=0;for(i=0;i&lt;256;++i){j=(j+this.S[i]+key[i%key.length])&amp;255;t=this.S[i];this.S[i]=this.S[j];this.S[j]=t;}this.i=0;this.j=0;}function ARC4next(){var t;this.i=(this.i+1)&amp;255;this.j=(this.j+this.S[this.i])&amp;255;t=this.S[this.i];this.S[this.i]=this.S[this.j];this.S[this.j]=t;return this.S[(t+this.S[this.i])&amp;255];}Arcfour.prototype.init=ARC4init;Arcfour.prototype.next=ARC4next;function prng_newstate(){return new Arcfour();}var rng_psize=256;var rng_state;var rng_pool;var rng_pptr;function rng_seed_int(x){rng_pool[rng_pptr++]^=x&amp;255;rng_pool[rng_pptr++]^=(x&gt;&gt;8)&amp;255;rng_pool[rng_pptr++]^=(x&gt;&gt;16)&amp;255;rng_pool[rng_pptr++]^=(x&gt;&gt;24)&amp;255;if(rng_pptr&gt;=rng_psize)rng_pptr-=rng_psize;}function rng_seed_time(){rng_seed_int(new Date().getTime());}if(rng_pool==null){rng_pool=new Array();rng_pptr=0;var t;while(rng_pptr&lt;rng_psize){t=Math.floor(65536*Math.random());rng_pool[rng_pptr++]=t&gt;&gt;&gt;8;rng_pool[rng_pptr++]=t&amp;255;}rng_pptr=0;rng_seed_time();}function rng_get_byte(){if(rng_state==null){rng_seed_time();rng_state=prng_newstate();rng_state.init(rng_pool);for(rng_pptr=0;rng_pptr&lt;rng_pool.length;++rng_pptr)rng_pool[rng_pptr]=0;rng_pptr=0;}return rng_state.next();}function rng_get_bytes(ba){var i;for(i=0;i&lt;ba.length;++i)ba[i]=rng_get_byte();}function SecureRandom(){}SecureRandom.prototype.nextBytes=rng_get_bytes;function parseBigInt(str,r){return new BigInteger(str,r);}function linebrk(s,n){var ret='';var i=0;while(i+n&lt;s.length){ret+=s.substring(i,i+n)+'\\n';i+=n;}return ret+s.substring(i,s.length);}function byte2Hex(b){if(b&lt;0x10)return '0'+b.toString(16);else "
				+ " return b.toString(16);}function pkcs1pad2(s,n){if(n&lt;s.length+11){return null;}var ba=new Array();var i=s.length-1;while(i&gt;=0&amp;&amp;n&gt;0){var c=s.charCodeAt(i--);if(c&lt;128){ba[--n]=c;}else if((c&gt;127)&amp;&amp;(c&lt;2048)){ba[--n]=(c&amp;63)|128;ba[--n]=(c&gt;&gt;6)|192;}else{ba[--n]=(c&amp;63)|128;ba[--n]=((c&gt;&gt;6)&amp;63)|128;ba[--n]=(c&gt;&gt;12)|224;}}ba[--n]=0;var rng=new SecureRandom();var x=new Array();while(n&gt;2){x[0]=0;while(x[0]==0)rng.nextBytes(x);ba[--n]=x[0];}ba[--n]=2;ba[--n]=0;return new BigInteger(ba);}function RSAKey(){this.n=null;this.e=0;this.d=null;this.p=null;this.q=null;this.dmp1=null;this.dmq1=null;this.coeff=null;}function RSASetPublic(N,E){if(N!=null&amp;&amp;E!=null&amp;&amp;N.length&gt;0&amp;&amp;E.length&gt;0){this.n=parseBigInt(N,16);this.e=parseInt(E,16);}else"
				+ " alert('Invalid RSA public key');}function RSADoPublic(x){return x.modPowInt(this.e,this.n);}function RSAEncrypt(text){var m=pkcs1pad2(text,(this.n.bitLength()+7)&gt;&gt;3);if(m==null)return null;var c=this.doPublic(m);if(c==null)return null;var h=c.toString(16);if((h.length&amp;1)==0)return h;else return '0'+h;}RSAKey.prototype.doPublic=RSADoPublic;RSAKey.prototype.setPublic=RSASetPublic;RSAKey.prototype.encrypt=RSAEncrypt;this.RSAKey=RSAKey;}).call(sinaSSOEncoder);function getpass(pwd,servicetime,nonce,rsaPubkey){var RSAKey=new sinaSSOEncoder.RSAKey();RSAKey.setPublic(rsaPubkey,'10001');var password=RSAKey.encrypt([servicetime,nonce].join('\\t')+'\\n'+pwd);return password;}";
 
		;
 
		return js;
	}
 
    //用户名编码
	private static String encodeAccount(String account) {
		String userName = "";
		try {
			userName = Base64.encodeBase64String(URLEncoder.encode(account,"UTF-8").getBytes());
 
		} catch (UnsupportedEncodingException e) {
 
			e.printStackTrace();
		}
		return userName;
	}
 
    //参数nonce值生成
	private static String makeNonce(int len) {
		String x = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		String str = "";
		for (int i = 0; i &lt; len; i++) {
			str += x.charAt((int) (Math.ceil(Math.random() * 1000000) % x
					.length()));
		}
		return str;
	}
