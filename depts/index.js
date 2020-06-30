const express=require('express');
const app=express();

app.use(express.json());
function conlog(m,q){
    console.log(m,'originalUrl=',q.originalUrl
        ,' ,body=',q.body,' ,params='
        ,q.params,new Date());} // q.signedCookies , q.subdomains

app.get('/test/:dt',(req,resp,next)=>{
    conlog('get/test:',req);
    try{let uid=req.get("usrId")
        ,results='ok'
        ,n=now()
        ,x={time:n,"return":results}
        resp.json(x);
    }catch(ex){
        console.log(ex);
        resp.sendStatus(500)
    }
})

app.use(express.static('./web/'))
app.listen(process.env.PORT || 80,()=>{
    console.log('Server is running on port 80 ,v2020-06-30-22-36');
});
