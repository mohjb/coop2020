/**
 * Created by mohamadjb@gmail.com on 16/4/20.
 */
package moh.coop;
/*
javac -d ../../WEB-INF/classes/ -cp /Users/moh/Google\ Drive/air/apache-tomcat-8.0.30/lib/jsp-api.jar:/Users/moh/Google\ Drive/air/apache-tomcat-8.0.30/lib/servlet-api.jar:/Users/moh/Google\ Drive/air/apache-tomcat-8.0.30/lib/el-api.jar:/Users/moh/Google\ Drive/air/apache-tomcat-8.0.30/lib/mysql-connector-java-5.1.43-bin.jar:/Users/moh/Google\ Drive/air/apache-tomcat-8.0.30/webapps/ROOT/WEB-INF/lib/commons-fileupload-1.2.1.jar:/Users/moh/Google\ Drive/air/apache-tomcat-8.0.30/webapps/ROOT/WEB-INF/lib/commons-io-1.4.jar Dbg.java M.java CoopSrvlt.java
* */

import java.lang.reflect.Field;
import java.util.*;

public class CoopSrvlt extends M {

@HttpMethod(usrLoginNeeded = false) public static Map
signup(@HttpMethod(prmName="cid") String cid
	, @HttpMethod(prmName ="pw") String pw //, @HttpMethod(prmName = "coopId") Number coopId//, @HttpMethod(prmName = "lastModified") Date lm,@HttpMethod(prmName = "clientTime")Date clientTime
	,@HttpMethod(prmName  ="val")Map val
	, TL tl)throws Exception {
	User u=User.load(cid);
	Map m= Util.mapCreate("time",tl.now,"return",false);
	Object o=val==null?null:val.get("coopId");
	Coop c=val==null||o==null?null:Coop.load(o.toString());
	if(u == null && c!=null ) {
		String md5 = Util.md5 (Util.b64d(pw));
		tl.h.s("usr",tl.usr=u=new User());
		tl.h.s("cid",u.id=cid);u.cat="usr";
		List l=(List)c.vo("signupReq");
		if(l==null)
			c.v("signupReq",l=Util.lst());
		if(!l.contains(cid))
		{l.add(cid);
			c.v("signupReq",l,true);}
		u.setVal(val);
		u.v("state",User.State.SignupPending.toString());
		u.v("cid",u.id=cid);
		u.m("pw",md5);
		u.insert();
		Util.mapSet(m,"return",u.state,"cid",cid);
	}return m;}

@HttpMethod(usrLoginNeeded = false) public static Map
login(@HttpMethod(prmUrlPart = true) String cid//prmName="cid"
	,@HttpMethod (prmUrlPart = true)Date lm//prmName ="lm"
	,@HttpMethod (prmUrlPart = true)Date ct //prmName="clientTime"
	, @HttpMethod(prmName="pw") String pw
	, TL tl)throws Exception {
	tl.log("CoopSrvlt.login:0:",cid,pw,lm,ct);
	User u=User.load (cid);
	String md5=pw != null?Util.md5(Util.b64d(pw)):pw,upw=u.pw();
	if(u != null && upw!=null && upw.equals(md5)) {
		tl.h.s("cid",cid);
		tl.h.s("usr",tl.usr=u);
		u.v("login",tl.now);
		u.v("session",ct,true); //m.put("return",tl.now);
		tl.log("CoopSrvlt.login:1:ok:");
		return Util.mapCreate("val",u.v(),"byDates",Prop.byDates(lm,null,tl));
	}tl.log("CoopSrvlt.login:2:return null;");return null;}

@HttpMethod public static Map
chngPw(@HttpMethod(prmName = "pw") String pw
	,@HttpMethod(prmName = "clientTime")Date clientTime
	, TL tl)throws Exception {
	//Map m= Util.mapCreate("time",tl.now);//String md5=Util.md5(pw);
	if(tl.usr != null ) { // && tl.usr.pw!=null && tl.usr.pw.equals(md5)
		tl.usr.chngPw(Util.b64d(pw));
		return Util.mapCreate("time",tl.now);
	}return null;}

@HttpMethod public static void logout(TL tl){
	tl.usr.v("logout",tl.now,true);
	tl.h.s("cid",tl.h.s("usr",tl.usr =null));}

@HttpMethod public static Map //update
put(@HttpMethod(prmUrlPart = true )String cat // prmName="cat"
    ,@HttpMethod(prmUrlPart = true)String id // prmName="id"
    ,@HttpMethod(prmUrlRemaining = true)List path // prmName = "path"
    ,@HttpMethod(prmBody = true)Object p // prmName = val
	,TL tl)throws Exception {
	Map m=Util.mapCreate();
	int typ="coop".equals(cat)?2:"usr".equals(cat)?1:0;
	Prop t=typ==2?Coop.  load(id):typ==1?User.load(id):Prop.tl();
	if(typ==0){t.cat=cat;t.id=id;t.load();}
	User.AccessLevel u=t.access(tl.usr.id);
	//if( u.ordinal()>User.AccessLevel.Load.ordinal()) {
		if(path != null && path.size() >= 1) {
			path.add(p);
			Util.bPath(t.v(), true, true, path.toArray());
		}
		t.save();
		m.put("return", tl.now);
	//}
	return m;

/**
 data: {<*cat>:{<*prop>:{<*path:string:dot-seperated>:<val:json>}}}
 * /
 @HttpMethod(usrLoginNeeded = false) public static Map
 updates(@HttpMethod(prmName = "clientTime")Date clientTime
 ,@HttpMethod(prmName = "data")Map p
 , TL tl)throws Exception
 {Map m=Util.mapCreate();
 Prop t=Prop.tl();
 for(Object ck:p.keySet()){

 }
 return m;}


 user-access-control
    consumer
        only consumer-profile , except coopId
        readOnly coopId and coop.publicData

    emp
        InOut
        empSignupApproveal
        empAppointmentApproval
        coopAdmin(Profile,config)
        coopUsersAdmin(addingEmp, edit access-control)



 */
}

@HttpMethod public static Map // updateMeta
patch(@HttpMethod(prmUrlPart = true )String cat // prmName="cat"
	,@HttpMethod(prmUrlPart = true)String id // prmName="id"
	,@HttpMethod(prmUrlRemaining = true)List path // prmName = "path"
	,@HttpMethod(prmBody = true)Object p // prmName = "meta"
	,TL tl)throws Exception {
	Map m=Util.mapCreate();
	int typ="coop".equals(cat)?2:"usr".equals(cat)?1:0;
	Prop t=typ==2?Coop.  load(id):typ==1?User.load(id):Prop.tl();
	if(typ==0){t.cat=cat;t.id=id;t.load();}

	User.AccessLevel u=t.access(tl.usr.id);
	if( u.ordinal()>User.AccessLevel.Load.ordinal()) {
		if(path != null && path.size() >= 1) {
			path.add(p);
			Util.bPath(t.m(), true, true, path.toArray());
		}
		//t.updateMetaFromM();t.save();
		m.put("return", tl.now);
	}return m;}

@HttpMethod public static Map // insert
post(@HttpMethod(prmUrlPart = true )String cat // prmName="cat"
	,@HttpMethod(prmUrlPart = true)String id // prmName="id"
	,@HttpMethod(prmBody = true)Object p // prmName="val"
	,TL tl)throws Exception {
	Map m=Util.mapCreate();
	int typ="coop".equals(cat)?2:"usr".equals(cat)?1:0;
	Prop t=typ==2?new Coop():typ==1?new User():Prop.tl();
	t.cat=cat;t.id=id;
	if(t.exists(t.wherePK(),null))
		return null;

	User.AccessLevel u=t.access(tl.usr.id);
	//if( u.ordinal()>User.AccessLevel.Load.ordinal()) {
		if(typ == 2)
			Coop.coops.put(id, (Coop) t);
		t.setVal(p);//(new Json.Output()).o(p).toString()t.m=Util.mapCreate("creatorCID",tl.usr.cid);t.updateMetaFromM();
		t.insert();
		m.put("return", tl.now);//}
	return m;}

@HttpMethod public static Map//byDates
get(@HttpMethod(prmUrlPart = true)Date d1//prmName="from"
	   ,@HttpMethod(prmUrlPart = true)Date d2//prmName="to"
	   ,TL tl)throws Exception{	return Prop.byDates( d1,d2,tl );}

//////////////////////////////////////////////////////////////////////

/**Object Store Entity entry/row*/
public static class Prop extends Sql.Tbl {
	final static String dbtName="Prop";
	@F public Date log ;
	@F public String cat,id;
	@F public Object meta;//Map v,m;
	@F public Object val;
	@Override public String getName() {return dbtName;}

	public enum C implements CI{log,cat,id,meta,val;
		@Override public Field f(){return Co.f(name(), Prop.class);}
		@Override public String getName() { return super.name(); }
		@Override public Class getType() { return this==log?Date.class:String.class; } // f().getType()
	}//C

	@Override public C[]columns(){return C.values();}

	@Override public Object[] wherePK() { return where(C.cat,cat,C.id,id); }

	@Override public List DBTblCreation(TL tl){
		final String T="LongText",V=
		"varchar(255) NOT NULL DEFAULT '??' ";
		return Util.lst(Util.lst(
				"TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"
				,V,V,T,T)
			,Util.lst("primary key(`"+C.cat+"`,`"+C.id+"`)",Util.lst(C.log))
			,Util.lst(Util.lst(tl.now,"usr","root","{pw:\"\"}","{name:\"moh\",cid:\"root\",tel:\"99876454\",email:\"mohamadjb@gmail.com\",coopId:\"1\",govId:\"\",areaId:\"\",blockId:\"\",branchId:\"1\"}")
				,Util.lst(tl.now,"coop","1","{pw:\"\"}","{label:\"main-coop\",cid:\"1\",tel:\"99876454\",email:\"mohamadjb@gmail.com\",branchs:[],govId:\"\",areaId:\"\",blockId:\"\"}")
			)
		);//val
		/*
		CREATE TABLE `Prop` (
		`id`	int(36) not null primary key auto_increment
		,`log`	TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
		,`cat`	varchar(255) NOT NULL DEFAULT '??'
		,`prop`	varchar(255) NOT NULL DEFAULT '??'
		,`meta`	text
		,`val`	text
		,unique(`cat`,`prop`)
		,unique(`log` )
		) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `Prop` (`id`	int(36) not null primary key auto_increment,`log`	TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,`cat`	varchar(255) NOT NULL DEFAULT '??',`prop`	varchar(255) NOT NULL DEFAULT '??',`meta`	text,`val`	text,unique(`cat`,`prop`),unique(`log` )) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `log` (
`log` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
`cat` varchar(255) NOT NULL DEFAULT '??',
`id` varchar(255) NOT NULL DEFAULT '??',
`meta` longtext,
`val`  longtext,
PRIMARY KEY (`cat`,`id`,`log`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


		*/
	}

	static{registered.add(Prop.class);}

	@Override public Sql.Tbl save() throws Exception {
		if(log==null)log=new Date();
		super.save();
		saveLog(null);
		return this;
	}//save

@Override public Sql.Tbl insert() throws Exception {
		if(log==null)log=new Date();
		super.insert();
		if(!(this instanceof Log))saveLog("insert");
		return this;
	}//save

	void saveLog(String op) throws Exception {
		Log g=Log.tl();
		g.vals( valsForSql() ); // g.log=log;g.cat=cat;g.id=id;g.meta=meta;g.val=val;
		try{
			if(op!=null)g.m("op",op);
			g.m("cid",User.tl().id);
			g.insert();
		}catch(Exception x){}
	}//saveLog

	public static void save(String cat,String id,String meta,String val)throws Exception{
		Prop m=Prop.tl();
		m.cat=cat;m.id=id;m.setVal(val);m.setMeta(meta);
		m.save();
	}

	public static Prop tl(){
		TL t=TL.tl();
		if(t.prop==null){
			t.prop=new Prop();Log.tl();check(t);//x.checkDBTCreation();
		}
		return t.prop;}

	static Map byDates( Date d1,Date d2, TL tl)throws Exception{
	if(tl.usr==null||d1==null)return null;
		Map m=Util.mapCreate();Prop t=tl();
		Object[]where=d2==null
			?where( //C.usr,u,
				Util.lst( C.log,Co.gt),d1)
			:where(//C.usr,u,
				Util.lst( C.log,Co.gt)
				,d1 ,Util.lst( C.log,Co.le ),d2
			);
		for(Sql.Tbl r:t.query( where ))try{
			int[]a=Util.dt2array( t.log );
			Util.byPath( m,true,true
				,a[0],a[1],a[2],a[3],a[4],a[5],a[6]
				,t.cat,t.id,t.val // v() // ==null?t.val:t.v // TODO: check t.m().access / t.accessLevel():AccessLevel
				);}catch(Exception x){
					tl.error(x,"Prop.byDates:query:",r);
				}
		return m;}


	Object setVal(Object p){
		val=p;
		if(p instanceof String)
			try{val=Json.Prsr.parse(p.toString());}catch(Exception x){}
		return p;
	}

	public Map v()throws Exception{
		if(val==null)
			val=new HashMap();
		else if(val instanceof Map)
			return (Map)val;
		else try{
				val=(new Json.Prsr()).parse(val.toString());
				if(val instanceof Map)
					return (Map)val;
				TL.tl().log("Prop.v:val not Map:",val);
			}catch(Exception ex){}
		return null;}

	public Object vo(String n)throws Exception{
		return v().get(n);}

	public String v(String n)throws Exception{
		Object o=v().get(n);
		String s=o==null?null:o.toString();
		return s;
	}

	public Object v(String n,Object x){return v(n,x,false);}
	public Object v(String n,Object x,boolean save){
		try{v().put(n,x);
		if(save)
			save();
		}catch(Exception ex){}
		return x;}

	Object setMeta(Object p){
		meta=p;
		if(p instanceof String)
			try{meta=Json.Prsr.parse(p.toString());}catch(Exception x){}
		return p;
	}

	public Map m(){
		if(meta==null)
			meta=new HashMap();
		else if(meta instanceof Map)
			return (Map)meta;
		else try{
			meta=(new Json.Prsr()).parse(meta.toString());
			if(meta instanceof Map)
				return (Map)meta;
			TL.tl().log("Prop.m:meta not Map:",meta);
	}catch(Exception ex){}
	return null;}

	public Object mo(String n){
		return m().get(n);}

	public String m(String n){
		Object o=m().get(n);
		String s=o==null?null:o.toString();
		return s;}

	public Object m(String n,Object x){return m(n,x,0);}

	public Object m(String n,Object x,int lvl){
		m().put(n,x);
		if(lvl>0)try{
			if(lvl>1)
				save();
		}catch (Exception ex){}
		return x;}

	@Override Sql.Tbl v(Field p, Object v){
		String pn=p==null?null:p.getName();
		if(C.val.getName().equals(pn))
			setVal(v);
		else if(C.meta.getName().equals(pn))
			setMeta(v);else
		super.v(p,v);
		return this;
	}

	@Override public Object valForSql(CI f){
	  if(f==C.val || f==C.meta){
		String s=Json.Output.out( f==C.val ?val:meta );
		return s;
	}return super.valForSql(f);}

	/**what accessLevel does the current user have access to id through this object?*/
	public User.AccessLevel access(String id){
		User.AccessLevel x=User.AccessLevel.Non;
		return x;}


} // class Prop extends Tbl

public static class Log extends Prop{
	//in the future: meta will have additional(compared to Prop) entry:path , which is the array of sub-properties path
//in the near-future: meta will have additional(compared to Prop) entry:cid , which is the user that did the changes
	@Override public String getName() {return "Log";}

	void saveLog() throws Exception {}//saveLog

	@Override public List DBTblCreation(TL t){
		List x=super.DBTblCreation(t);
		x.set(1,Util.lst("primary key(`"+C.cat+"`,`"+C.id+"`,`"+C.log+"`)"));
		return x;
	}

	static{registered.add(Log.class);}

	public static Log tl(){
		TL t=TL.tl();
		if( t.propLog==null){
			t.propLog=new Log();check(t);
		}
		return t.propLog;
	}

} // class Log extends Prop extends Tbl

//////////////////////////////////////////////////////////////////////

public static class User extends Prop{
	Coop coop;State state;
	public static User tl(){
		TL t=TL.tl();return t==null?null:t.usr;}

	static User load(String cid){
		User u=new User(),x=null;
		for(Sql.Tbl t:u.query(where(C.id,cid,C.cat,"usr"),true))
			x=x==null
				?(User)t
				:x;
		//if(x!=null)try{x.cid=cid;x.pw=x.m("pw");x.state=State.valueOf(x.v("state"));}catch(Exception ex){}
		return x;}

	public String pw(){return m("pw");}
	public State state(){
		try{String x=v("state");
			return x==null
				?State.Non
				:State.valueOf(x);
		}catch(Exception ex){
		}
		return State.Non;}

	public User.AccessLevel access(String cid){
		try{String x=v("state");
			return x==null
				?AccessLevel.Non
				:AccessLevel.valueOf(x);
		}catch(Exception ex){
		}
		return AccessLevel.Non;}

	public boolean chngPw(String pw){
		String x=Util.md5(pw);
		m("pw",x,2);
		return true;}

	public boolean chngCoopId(String coopId){
		v("coopId",coopId,true);
		return true;}

	@Override public Sql.Tbl save() throws Exception {
		cat="usr";return super.save();}
	@Override public Sql.Tbl insert() throws Exception {
		cat="usr";return super.insert();}

	@Override public Object v(String n,Object x,boolean save){
		if("state".equalsIgnoreCase(n)){
			try{state=x instanceof State?(State)x:State.valueOf(x.toString());}catch(Exception ex){}
			x=state==null?null:state.toString();
		}else if("pw".equalsIgnoreCase(n)){
			return null;//x=state==null?null:state.toString();
		}
		return super.v(n,x,save);
	}

public enum State{Non
	,SignupPending
	,ProfilePending
	,Ready  //  ,Loggedout
	,QueuePending
	,Queued
	,Auth2enter
	,InCoop
	,QuotaPending
} // enum State

public enum AccessLevel{Non
	,List   //assets: emp,coop,access
	,Load
	,Edit
	,Admin //,Write,Create,Delete
	,Super // EditAccess , createUser
}

public enum UserRole{consumer,inOut,signupApproveal,appointmentApproval,coopAdmin,coopUsersAdmin}

}//class User extends Prop extends Tbl

//////////////////////////////////////////////////////////////////////

public static class Coop extends Prop{

	public static Map<String,Coop>coops;

	public static Coop tl(){
		User t=User.tl();return t==null?null:t.coop;}

	static void loadCoops(){
		if(coops==null)coops=new HashMap();
		Coop x=new Coop(),c;
		for(Sql.Tbl t:x.query(where(C.cat,"coop"),true)){
			c=(Coop)t;
			coops.put(c.id,c);}}

	static Coop newCoop(String name,Map v){
		Coop x=new Coop();
		x.id=String.valueOf(TL.tl().now.getTime());
		x.setVal(v);
		String cid=null;
		try{cid=User.tl().v("cid");}catch(Exception xc){}//x.v("creatorCID",cid);
		if(name!=null)x.v("label",name);
		List l=(List)v.get("emps");
		if(l==null)
			x.v("emps",l=Util.lst());
		if(!l.contains(cid))l.add(cid);
		x.v("created",TL.tl().now,true);
		return x;}

	static Coop load(String id){
		if(id==null)return null;
		if(coops==null)loadCoops();
		Coop x=coops.get(id);
		return x;}

	@Override public Sql.Tbl save() throws Exception {
		cat="coop";return super.save();}
	@Override public Sql.Tbl insert() throws Exception {
		cat="coop";return super.insert();}

	//List generateApts(Date day){}

	Map emps(){
		Object o=m("emps");
		Map m=o instanceof Map?(Map)o:null;
		return m;}

	List consumers(){
		Object o=m("consumers");
		List m=o instanceof List?(List)o:null;
		return m;}

	@Override public User.AccessLevel access(String cid){
		Map e=emps();
		Object o=e==null?e:e.get(cid);
		return o instanceof User.AccessLevel
			?(User.AccessLevel)o
			:User.AccessLevel .Non;}

}//class Coop extends Prop

public static CoopSrvlt sttc=new CoopSrvlt();

}// CoopSrvlt
