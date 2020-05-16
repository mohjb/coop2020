now=()=>new Date().getTime();
b64e=s=> Buffer.from(s).toString('base64');
b64d=s=> new Buffer(s,'base64').toString();

const express=require('express');
const app=express();
const mysql=require('mysql');
const pool= mysql.createPool({ password:'m',user:'moh',database:'coop'}); // ,host:'localhost',port:'3306',connectionLimit:10
let db={db:{},session:{}
//,all:(  )=>new Promise((resolve,reject)=>{pool.query('select * from prop',(err,result)=>{if(err)return reject(err);return resolve(result);})})
,init:()=>{
	pool.query('select * from prop ',(err,result)=>{
		if(err)
			return reject(err)
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
	})
}
,md5:async function(s){
	let f=x=>{pool.query('select md5(?) as r',x
		,(err,result)=>{
		if(err)
			return reject(err)
		return resolve(result[0].r);
	})}
	let r=await f(s)
	return r;
}
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
	pool.query('insert into `log` values(now(),?,?,?,?,?)'
	,[cat,id,prop,JSON.stringify(meta),JSON.stringify(val)]
	,(err,result)=>{
		if(err)
			return reject(err)
		return resolve(result);
	})}
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

*/
