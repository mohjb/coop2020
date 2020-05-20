now=()=>new Date().getTime();
b64e=s=> Buffer.from(s).toString('base64');
b64d=s=> new Buffer(s,'base64').toString();

Date.fields=['FullYear','Month','Date','Hours','Minutes','Seconds','Milliseconds']

Date.fromArray=function DateFromArray(da,i){
	let dt=new Date(0);
	Date.fields.forEach((fj,j)=>{
		if(j>i)return;
		let fa1=da[j]
			,b=j==1 // offset
			,fa=b?fa1-1:fa1
			,t=dt
			,mthdNm='set'+fj
			,fnc=t[mthdNm]
			,far=fnc.call(dt,fa);
	}
	);
	return dt;
	}
Date.prototype.toArray=function DateToArray(){
	let x=this
	,r= Date.fields.map((f,i)=>{
		let ta0=i==1?1:0
			,mthdNm='get'+f
			,fnc=x[mthdNm]
			,ta1=fnc.apply(x)
			,ta=ta1+ta0;
		return ta;});
	return r;
	}

const express=require('express');
const app=express();
const fs=require('fs');
//const crypto = require('crypto');

///////////////////////////////
String.prototype.hash=function hash(p){
	if(!p)p=this;//return '';
	let r=[],n=hash.n,b=hash.b,a=hash.a
	,pn=p.length,c=p.charCodeAt(0),d=c%62
	if(!n){n=hash.n=24;b=hash.b=[]
		let u=[['0','9'],['A','Z'],['a','z']]
		for(var j=0;j<u.length;j++){
			var x=u[j]
			,c0=x[0].charCodeAt(0)
			,c1=x[1].charCodeAt(0)
			for(var k=c0;k<=c1;k++)
				b.push(k);}
		b.push('$'.charCodeAt(0),'_'.charCodeAt(0));
		let bitRotate=hash.bitRotate=(byte,i)=>{
			if(!i)i=1;
			let a=((byte&255) << i )  &255
				, b = ((byte&255) >>(8-i))&255
				,r=a|b
				return r;}
		,bitSwapNibbles=(byte)=>{
			let a=((byte&15)<<4) 
			,b= ((byte&255) >>4)&15
			,r=a|b
			return r;}
		,bitSwapEvenOdd=(byte)=>{
			let a=((byte&0x33)<<1) &255	// 0101 0101
			,b = ( (byte&0x66)>>1) &255	// 1010 1010
			,r=a|b
			return r;}
		,bitReverse=(byte)=>{
			let r=0;
			for(let i=0;i<8;i++)
			  if(byte&(128>>i))
				r=r|(1<<i)
			return r;}
		a=hash.a=[bitReverse,bitSwapEvenOdd,bitSwapNibbles]
	}
	for(var j=0;j<n;j++)r[j]=d;

	function propagate(c,i){
		propgt=(x,i)=>{
		  for(let j=i;j<i+n;j++){
		  	let i1=j%n,v1=r[i1],v2=x++,v3=v1+v2
		  		,v4=v3%64;
			if(!r.includes(v4))r[i1]=v4;}}

		let c2,j=8,g=-1;
		for(let i0=i;i0<i+n;i0++){
			if(++j>7){j=0;
				c2=a[++g%a.length](c);}
			else c2=hash.bitRotate(c2)
			if(i0==i+n-1)
				i0=i0 +0;
			propgt(c2,i0)
		}
	}


	for(var i=0;i<p.length;i++)
		propagate(p.charCodeAt(i),i);

	for(var j=0;j<n;j++)r[j]=String.fromCharCode( b[r[j] ] );
	return r.join('');
} // function hash
/////////////////////////////////

db={db:{},logCache:{bounds:[0,0]},session:{}

,init:()=>{let log=db.logCache.bounds;
	function f(pathArr){
		function fil(pth,obj,prop){
			fs.promises.readFile(pth,'utf-8').
			then(x=>{if(prop=='meta')
				obj.meta=JSON.parse(x)
				else obj.val[prop]=JSON.parse(x)
				fs.promises.lstat(pth,'utf-8').
				then(x=>db.loglog(obj,prop,null,x.mtimeMs))
			})
		}
		fs.promises.readdir(pathArr.join('/'),{withFileTypes:true}).then(a=>{
			for (var i=0;i<a.length;i++) {
				//console.log(file);
				let x=a[i]
				if(x.isDirectory())
					f(pathArr.concat(x.name))
				else{let n=pathArr.length
					,catId=pathArr[n-3]
					,id=pathArr[n-2]
					,prop=pathArr[n-1]
					,cat=db.db[catId],obj,pth;
					if(!cat)cat=db.db[catId]={}
					pth=pathArr.concat(x.name).join('/');
					obj=cat[id];if(!obj)
						obj=cat[id]={cat:catId,id:id,log:0,meta:{},val:{},propLog:{}}
					fil(pth,obj,prop);
				}
			}
		})
	}
	f(['.','db','prop']);
}
,loglog:(obj,prop,da,lm)=>{
	function rem(g){
		let p=g.parent;
		if(!p)return;
		delete p[g.key];
		let k=Object.keys(p)
		if(k.length<=3)
			rem(p);}
	if(!da)da=new Date(lm||obj.log).toArray();
	if(!lm)lm=obj.log;else
	if(obj.log<lm)obj.log=lm;
	if(obj.propLog[prop])
		rem(obj.propLog[prop])//delete obj.propLog[prop];
	let g=[],i=0
	while(i<da.length){
		g[i]=(i==0?db.log:g[i-i])[da[i]]
		if(!g[i]){g[i]=
			{parent:g[i-1],key:da[i],t:Date
				.fromArray(da,i).getTime()}
			if(i==0){
				db.logCache[da[0]]=g[0];
				g[0].parent=db.logCache}
			}
		i++;}
	g=g[g.length-1]
	if(g.obj){i=da.length-1;
		let p=g.parent,x=p[++da[i]]
		while(x)
			x=p[++da[i]];
		p[da[i]]=g={parent:g[i-1],key:da[i]
			,t:Date.fromArray(da,i).getTime()}
	}
	g.obj=obj;
	g.prop=prop;
	g.da=da;g.t=lm;
	if(obj.propLog[prop])
		rem(obj.propLog[prop]);//g
	obj.propLog[prop]=g
	let log=db.logCache.bounds;
	//if(log[0]==g){log[0]=null;}		// TODO: if log[0] is relocated , must find the next minimum ; if(!log[0] || log[0].t>lm) log[0]=g;
	//if(log[1]==g){}		// T-O-D-O: if log[1] is relocated, must find the previous maximum
	if(!log[1] || log[1].t<lm)
		log[1]=g;
	}
,byDate:(dt)=>{
	let o={} // ,d=new Date(dt).toArray(),
	function f(p,a,lvl){
		for(let k in p){let g=p[k]
			if(dt>g.t){
				if(!g.obj)//lvl<7)
					f(g,a.concat(k),lvl+1)
				else{let x=g.obj
					,cat=o[x.cat]||(o[x.cat]={})
					,id=cat[x.id]||(cat[x.id]={})
					if(!id[x.prop] || id[x.prop].t<x.t)
					id[x.prop]={t:x.t,val:db.db[x.cat][x.id].val[x.prop]}
				}
			}
		}
	}
	f(db.logCache,[],0)
	return o;
	}

,save:( obj,prop,dirdate)=>
	fs.promises.mkdir('./db/prop/'+obj.cat+'/'+obj.id,{recursive:true}).then(()=>
		fs.promises.writeFile('./db/prop/'+obj.cat+'/'+obj.id+'/'+(prop==null?'.obj':fnm)
		, JSON.stringify(prop==null?obj.val:prop=='meta'?obj.meta: obj.val[prop])))

,access:(cid,cat,id)=>{//returnValue::= 0:no-access , 1:read-only , 2:write , 3:admin , 4:access
	let r=0,d=db.db,a=[d[cat]],m
	if(a[0]){
		a[1]=a[0][id]
		if(a[1]){
			m=a[1].meta
			if(m && m.access)
				r= m.access[cid]
			if(cat=='usr' && id==cid&& r<1)
				r=1;
		}
	}return r;
}
,put:(cat,id,prop,val)=>{
		if(!db.db[cat] || !db.db[cat][id])return null;
		let o=db.db[cat][id]
		o.val[prop]=val;
		db.save(cat+'/'+id,o,prop)
		db.log(cat,id,prop,{op:'put',cid:null},val)
		db.loglog(o,prop)
		return o;
	/*pool.query('replace into prop values(now(),?,?,?,?)'
	,[cat,id,meta,val],(err,result)=>{
		if(err)
			return reject(err)
		return resolve(result);
	})*/	}
,post:(cat,id,prop,val)=>{
	if(db.db[cat] && db.db[cat][id])return null;
	if(!db.db[cat])db.db[cat]={}
	let o=db.db[cat][id]={cat:cat,id:id,log:now(),val:{}}
	o.val[prop]=val;
	db.save(cat+'/'+id,o,prop)
	db.log(cat,id,prop,{op:'post',cid:null},val)
	db.loglog(o,prop)
	return o;
	}
,log:(cat,id,prop,meta,val)=>{
	let t=new Date()
	,da=t.toArray(),d=da.join('/')
	,s='./db/log/'+d+'/'+cat+'/'+id+'/';
	fs.promises.mkdir(s,{recursive:true}).then(()=>
		fs.promises.writeFile(s+prop, JSON.stringify({
			cat:cat,id:id,log:t.getTime()
			,meta:meta,prop:prop,content:content
		})));
	db.loglog(db.db[cat][id],prop,da);}
};//db


app.use(express.json());

app.get('/poll/:dt',(req,resp,next)=>{
	console.log('get/poll:',req,resp,next);
	try{let results=db.byDate(req.params.dt)
		let x={time:now(),props:results}
		resp.json(x);
	}catch(ex){
		console.log(ex);
		resp.sendStatus(500)
	}
})

app.put('/login/',(req,resp,next)=>{
	console.log('get/login:',req,resp,next);
	try{let b=req.body,uid=b.usrId,pw=b64d(b.pw).hash(),exp=b.expire
		, u=db.db.usr[uid]
		,um=u&&u.meta&&u.meta.pw
		,ux=u&&u.val&&u.val.expire
		,x=0;if(u &&((ux&&exp==ux) || (um&&um==pw))){
			x={time:now(),log:u.log,val:u.val}
			let s=db.session[uid];
			if(!s)s=db.session[uid]={u:u,login:x.time};
			u.val.login=x.time;
			db.save('usr/'+uid,u,'login');
			db.loglog(u,'login',null,x.time);
		}
		resp.json(x);
	}catch(ex){
		console.log(ex);
		resp.sendStatus(500)
	}
})

app.post('/signup/',(req,resp,next)=>{
	console.log('get/signup:',req,resp,next);
	try{let b=req.body,uid=b.usrId,pw=b64d(b.pw).hash(),val=b.val
		, u=db.db.usr[uid]
		,x=null;
		if(!u){
			u=db.db.usr[uid]={cat:'usr',log:now(),id:uid,meta:{pwd:pw},val:val}
			x={time:now(),log:u.log,val:u.val}
			let s=db.session[uid];
			if(!s)s=db.session[uid]={u:u,login:x.time};
			u.val.login=x.time;
			db.save('usr/'+uid,u,'meta');
			db.loglog(u,'meta',null,x.time);
			for(let k in val){
				db.save('usr/'+uid,u,k);
				db.loglog(u,k,null,x.time);
			}
		}
		resp.json(x);
	}catch(ex){
		console.log(ex);
		resp.sendStatus(500)
	}
})

app.get('/logout/:dt',(req,resp,next)=>{
	console.log('get/logout:',req,resp,next);
	try{let results='await db.byDate(req.params.dt)'
		let x={time:now(),props:results}
		resp.json(x);
	}catch(ex){
		console.log(ex);
		resp.sendStatus(500)
	}
})

app.put('/:cat/:id',(req,resp,next)=>{
	console.log('put/',req,resp,next);
	try{let p=req.params,b=req.body,results='await db.put(p.cat,p.id,b.prop,b.val)'
		db.log(p.cat,p.id,b.prop,{op:'update',req:req},b.val)
		let x={time:now(),props:results}
		resp.json(x);
	}catch(ex){
		console.log(ex);
		resp.sendStatus(500)
	}
})

app.post('/:cat/:id',(req,resp,next)=>{
	console.log('post',req,resp,next);
	try{let p=req.params,b=req.body,results='await db.post(p.cat,p.id,b.prop,b.val)'
		db.log(p.cat,p.id,b.prop,{op:'create',req:req},b.val)
		let x={time:now(),props:results}
		resp.json(x);
	}catch(ex){
		console.log(ex);
		resp.sendStatus(500)
	}
})

app.listen(process.env.PORT || 1024,()=>{
	console.log('Server is running on port 1024 ,v2020-05-13-18-12');
	db.init()
});

//console.log('indexJs : ',express,app);
