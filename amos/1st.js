amos= {
	man: {
		protoConstruction:{ //declProg
			init4Divs:function(parentDom,prms){}
			,initConstructProto:function(construct,prms){}
			,Construct1stProto:{
				// inits:
				init:function(parentCnstrct,fields){
					//init decl-4divs (in 1stImpl 1divs which is props )


				}

				//events: onOvr onExit onClck onMv onDrag onChng onFocus onBlur
				,onOvr:function(evt){}
				,onExit:function(evt){}
				,onClck:function(evt){}
				,onMv:function(evt){}
				,onDrag:function(evt){}
				,onChng:function(evt){}
				,onFocus:function(evt){}
				,onBlur:function(evt){}

				//fnctns: drawBorder updateDivs boundingbox
				,drawBorder:function(evt){}
				,updateDivs:function(evt){}
				,boundingbox:function(evt){}
			}//Construct1stProto
		}//protoConstruction
		,initPage:window.onload=function () {
			let b=document.body,am=amos.man,c=am.context,cnvs,cd=c.dom,d=am.dom;
			Object.keys(cd).forEach(k=>cd[k]=d.e(k=='canvas'?k:'div',b,k))
			(cnvs=cd.canvas).onresize=c.events.onResz;
			amos.outline=[new amos.Construct(amos)]
			cnvs.onmousemove=c.events.onMousMv
			cnvs.onmouseup=c.events.onMousUp
			cnvs.onmousedown=c.events.onMousDn
			c.events.intrvl=setInterval(c.events.onDraw,100)
		}//initPage
		,context:{ctx:0,dom:{canvas:0,opDiv:0,amosDiv:0}
			,events:{
				down:{//p::=ref to closest construct
					// m::=mouse-coords when mouse-down
					// c::= point-coords of closest when mouse-down
					// flag::= bool if mouse-button is currently down
					p:0,m:{x:0,y:0},pc:{x:0,y:0},flag:0}
				,closest:{p:0,dist:0
					,find:function(){
						let c=amos.man.context.events.closest,o=c.p //,oz=c.dist;
						c.dist=Number.MAX_VALUE;
						amos.outline.forEach(p=>{
							let z=p.dist();if(z<c.dist){
								c.dist=z;c.p=p;
							}});if(o!=c.p)console.log('closest=',c.p,',old=',o);
						return c.p;}
				} //
				,onResz:function(evt){
					let b=document.body,am=amos.man,c=am.context,cd=c.dom,d=am.dom;
					let p=cd.canvas
						,q=c.dim={w:p.width =document.body.clientWidth
						,h:dp.height = ocument.body.clientHeight}
					q.cx=q.w/2;q.cy=q.h/2;
				}
				,onDraw:function(evt){}
				,onMousMv:function(evt){}
				,onMousUp:function(evt){}
				,onMousDn:function(evt){}
			}//events	,funcs:{}//funcs
		}//context
		,dom:{did:id=> document.getElementById(id)
			,e:(nm,p,id,atrbs,styl,txt)=>{
				let e=document.createElement(nm);
				if(id)e.id=id;
				if(atrbs)Object.keys(atrbs).forEach(k=>e[k]=atrbs[k]);
				if(styl)Object.keys(styl).forEach(k=>e.style[k]=styl[k]);
				if(txt)amos.man.dom.txt(txt,e)
				if(p)p.appendChild(e);return e;}
			,txt:(txt,p)=>{let e=document.createTextNode(txt);if(p)p.appendChild(e);return e;}
			,inp:(typ,val,onchng,id,p,atrbs,styl)=>{
				let e=amos.man.dom.e(['textarea','select','button'].includes(typ)?typ:'input',0,id,atrbs,styl)
				if(val!=undefined)e.value=val;
				if(onchng)e.onchange=onchng
				if(p)p.appendChild(e);return e;}
			,fldst:(lgnd,c,p,atrbs,styl)=>{
				let f=amos.man.dom.e('fieldset',0,id,atrbs,styl)
					,g=amos.man.dom.e('legend',f,0,0,0,lgnd)
				if(c)e.value=val;
				if(onchng)e.onchange=onchng
				if(p)p.appendChild(e);return e;}
			,bld:()=>{}
		}//dom
	}//man

	,Construct:function(parent,fields) // currently 1st-implementation (Point)
	{//CompositeConstruct

	}//Construct
/**<breakdown stage0>
amos creator(2d - html5canvas)cell editor
  palete(Context2dPalete:Context2d creates a Construct2D )
	path(beginPath,stroke,fill)
		point(prop,isInPath, isInStroke,lineTo,moveTo)
		arc (at , to)
		ellipse
		rect (stroke,fill)
		curv (beizerTo , quadraticTo )
	group
	text(fill|stroke) (font , align , baseline) (measure)
	fill(each)stroke
		color / alpha
		width , lineWidth
		paint
		img(url | base64)
		gradient ( linear | radial )
		create
		pattern
		create
		filter
		shadow Blur,color,offset(x,y)
	line join,cap,dashoffset(prop,get,set),miterLimit
	transform(prop,get,reset, rotate, scale,translate,set )
	state(save,restore)
	hitRegion(prop,create,remove)
	imgData ( create ,get,put)
	template(brush,op)
	script(JSSyntax)<expr>
	<literals>
	number,string,bool(true false null undefined)
	obj //<scope|symbolTbl>(as a Construct2d has props: entries,where each entry has: point,key(text,offset),val)
	array (as a Construct2d(a grid) has props:gridCellOffset CoordSystem_Transform)
	new
	<operators>
	<Arithmetic>
	+ - * / ++ -- **
	ternirary
	<memberProp//arrIndex|dotNotation >
	<logical> && || !
	<compar>< <= > >= == !=
	<bitwise> & | ^ !!
	<assign> =
	<decl>
	funcDecl
	classDecl
	<builtin classes>Object , JSSyntax , Context2d , Array , String , Date , Math , Function , JSON , Dom(Window,Document,Element)
	funcCall
	<control>if,while,try,throw,return,break,switch,for
  outline
  canvas
  implicit-group (border , action-buttons , transform , op )
  op(mode: CoordSystem(cart|polar , layoutMan ) , selectionList , palete , outline/symbolTbl )

<breakdown stage1> (action-plan : steps)
  <op>
	 ,implicit-group / borderOps (
		 CoordSys(cartesian , polar)
		,transforms (translate, rotate, scale )
		,anim
		)
	dom divs(
		outline
		,palete
		,name-selections
		,entity-ops(
			clone
			,newSubObject
			,newProp
			,listProps
			,renamePropKey
			,json(stringify,parse)
		)
		,props
	)
	path
	group

<breakdown stage2>
  Group Op (CompositeConstruct)
	1. op dom-templates engine(DeclaritiveProgramming)
		4-divs (outline(tree , entity-ops ) , named-selections , palete , props )
	2. op implicit-group (borderOps)
		drawBorder
		,transforms (translate, rotate, scale )
		,CoordSys(cartesian , polar) (bools:ShowOffset )
	3. array grid LayoutOnCanvas
	4. obj LayoutOnCanvas
  Point Op (AtomicConstruct)
	1. op dom-templates engine(DeclaritiveProgramming)
		3-divs (outline(tree , entity-ops ) , named-selections , palete )
	2. op implicit-group (borderOps)
		drawBorder
		,transforms (translate, rotate, scale )
		,CoordSys(cartesian , polar) (bools:ShowOffset )
	3. array grid LayoutOnCanvas
	4. obj LayoutOnCanvas
*/

/**<breakdown stage3>
 Construct ,
 1st-implementation is a point
 2nd implementation is a group
 3rd: implementation-add outline and path
 4th: implementation-add borderOps
 5th: implementation-add array-grid LayoutOnCanvas
 6th: implementation-add obj LayoutOnCanvas
*/
} // amos
