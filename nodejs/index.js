now=()=>new Date().getTime();
b64e=s=> Buffer.from(s).toString('base64');
b64d=s=> new Buffer(s,'base64').toString();
Date.prototype.toArray=function DateToArray(){let x=this;return [x.getFullYear(),x.getMonth()+1,x.getDay(),x.getHours(),x.getMinutes(),x.getSeconds(),x.getMilliseconds()];}
const express=require('express');
const app=express();
const fs=require('fs');
//const crypto = require('crypto');

///////////////////////////////
function hash(p){
	if(!p)return '';
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



let db={db:{},session:{}
//,all:(  )=>new Promise((resolve,reject)=>{pool.query('select * from prop',(err,result)=>{if(err)return reject(err);return resolve(result);})})
,init:()=>{
	for(var i in result){
		let o=result[i]
		,cat=db.db[o.cat]
		if(!cat)
			cat=db.db[o.cat]={}
		let id=cat[o.id]
		if(!id)
			id=cat[o.id]={cat:o.cat,id:o.id,log:o.log}
		let pp=id[o.prop]
		id[o.prop]=JSON.parse(o.val);
	}
}

,save:(path, obj,prop,dirdate)=>
	fs.promises.mkdir('./db/prop/'+path,{recursive:true}).
	then(()=>
	fs.promises.writeFile('./db/prop/'+path+'/'+fnm
	, JSON.stringify(content)));

,load:(path,obj,prop,dirdate)=>
	fs.promises.readFile('./db/prop/'+path+'/'+prop,'utf-8').
	then(x=>obj[prop]=JSON.parse(x));

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
,byDate:(dt)=>{
	pool.query('select * from prop where `log`>?'
		,dt,(err,result)=>{
		if(err)
			return reject(err)
		return resolve(result);
	})
}
,put:(cat,id,prop,val)=>{
	pool.query('replace into prop values(now(),?,?,?,?)'
	,[cat,id,meta,val],(err,result)=>{
		if(err)
			return reject(err)
		return resolve(result);
	})}
,post:(cat,id,prop,val)=>{
	pool.query('insert into prop values(now(),?,?,?,?)'
	,[cat,id,meta,val],(err,result)=>{
		if(err)
			return reject(err)
		return resolve(result);
	})}

,log:(cat,id,prop,meta,val)=>{
	let d=new Date().toArray().;
	if(!d)d=db.log.d=date=>{let d=[date.];};
	}
};//db


app.use(express.json());

app.get('/poll/:dt',(req,resp,next)=>{
	console.log('get/poll:',req,resp,next);
	try{let results=await db.byDate(req.params.dt)
		let x={time:now(),props:results}
		resp.json(x);
	}catch(ex){
		console.log(ex);
		resp.sendStatus(500)
	}
})

app.get('/login/',(req,resp,next)=>{
	console.log('get/login:',req,resp,next);
	try{let b=req.body,uid=b.usrId,pw=db.md5(b64d(b.pw))
		, u=db.db.usr[uid]
		,um=u.meta&&u.meta.pw
		,x=0;if(um==pw){
			x={time:now(),log:u.log,val:u.val}
			
		}
		resp.json(x);
	}catch(ex){
		console.log(ex);
		resp.sendStatus(500)
	}
})

app.get('/signup/:dt',(req,resp,next)=>{
	console.log('get/signup:',req,resp,next);
	try{let results=await db.byDate(req.params.dt)
		let x={time:now(),props:results}
		resp.json(x);
	}catch(ex){
		console.log(ex);
		resp.sendStatus(500)
	}
})

app.get('/logout/:dt',(req,resp,next)=>{
	console.log('get/logout:',req,resp,next);
	try{let results=await db.byDate(req.params.dt)
		let x={time:now(),props:results}
		resp.json(x);
	}catch(ex){
		console.log(ex);
		resp.sendStatus(500)
	}
})

app.put('/:cat/:id',(req,resp,next)=>{
	console.log('put/',req,resp,next);
	try{let p=req.params,b=req.body,results=await db.put(p.cat,p.id,b.prop,b.val)
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
	try{let p=req.params,b=req.body,results=await db.post(p.cat,p.id,b.prop,b.val)
		db.log(p.cat,p.id,b.prop,{op:'create',req:req},b.val)
		let x={time:now(),props:results}
		resp.json(x);
	}catch(ex){
		console.log(ex);
		resp.sendStatus(500)
	}
})

app.listen(process.env.PORT || 80,()=>{
	console.log('Server is running on port 80 ,v2020-05-13-18-12');
	db.init()
});


console.log('indexJs : ',express,app);

/*

const mysql=require('mysql');
const pool= mysql.createPool({ password:'m',user:'moh',database:'coop'}); 
md5=async function(s){
	let f=x=>{pool.query('select md5(?) as r',x
		,(err,result)=>{
		rv=err||result;
	console.log('md5',rv);
		return rv;
	})}
	let r=await f(s)
	return r;
}

https://nodejs.org/api/crypto.html#crypto_crypto

*/

