amos= {
	man: {
		protoConstruction:{ //declProg
			init4Divs:function(parentDom,prms){}
			,Construct1stProto:{
				r:5
				,fields:['x','y','w','h']//,r,color,ovr,label.str,label.position,hashcode,parent,firstChild,lastChild,prv,nxt,outline,meta.grfc,meta.fields

				//events:
				,onchgDomProp:function (){
					let t=this.construct,n=this.name;
					t.setProp(n,this.value,1);
				}
				,onOvr:function(evt){this.ovr=evt;
					this.info('onOvr'+this);
					console.log('Construct1stProto.onOvr:',evt)}
				,onExit:function(evt){this.ovr=0;
					this.info('onExit'+this);
					console.log('Construct1stProto.onExit:',evt)}
				,onClk:function(evt){
					console.log('Construct1stProto.onClk:',evt)}
				,onMv:function(evt){
					//console.log('Construct1stProto.onMv:',evt)//this.info('onMv'+this);
				}
				,onDrag:function(evt){
					let t=amos.man.context.events.down;
					} // console.log('Construct1stProto.onDrag:',evt,t,t.m,t.pc)
				,onChng:function(evt){
					console.log('Construct1stProto.onChng:',evt)}
				,onFocus:function(evt){
					this.info('onFocus'+this);
					//console.log('Construct1stProto.onFocus:',evt)
				}
				,onBlur:function(evt){
					this.info('onBlur'+this);
					console.log('Construct1stProto.onBlr:',evt)}

				//fnctns: drawBorder updateDivs boundingbox
				,info:s=>amos.man.context.dom.info.amosDiv.innerText=s
				,initFields:function(){
					let t=this,m=t.meta={dom:{},vis:{}}
						,am=amos.man,d=m.dom,a=am.dom
						,c=am.protoConstruction.Construct1stProto
					d.propsDiv=a.e('div')
					t.fields.forEach(f=>{
						d[f+'Fldst']=a.fldst(f
							,d[f]=a.inp('number',t[f],t.onchgDomProp,f,0,{construct:t,name:f})
							,d.propsDiv,f)
					})
				}
				,getProp:function(nm){return this[nm];}
				,setProp:function(nm,val,isDontSetDom){
					let t=this;
					t[nm]=parseInt(val);
					if(!isDontSetDom)
						t.meta.dom[nm].value=val;
					return val;
					}
				,drawBorder:function(evt,ctx){
					let t=this,x=0;
					if(t.ovr){x=ctx.strokeStyle ;ctx.strokeStyle ='red';}
					ctx.beginPath();
						ctx.arc(t.x-t.r, t.y-t.r, t.r, 0, 2 * Math.PI);
					ctx.stroke();if(t.ovr)ctx.strokeStyle =x;
				}
				,boundingbox:function(evt){
					let t=this;return{x:t.x-t.r,y:t.y-t.r,w:t.r*2,h:t.r*2};}
				,isOvr:function(p){
					let t=this;return t.x<=p.x+t.r*2&&p.x<=(t.x+t.r)
						&& t.y<=p.y+t.r*2 && p.y<=(t.y+t.r);}
				,onDraw:function(evt,ctx){this.drawBorder(evt,ctx);}
				,distance:function(p){
					let t=this
						,q=p||amos.man.context.events.m
						,dx=t.x-q.x,dy=t.y-q.y,r=dx*dx+dy*dy
						,v=Math.sqrt(r);return v;}
				,toString:function(){let t=this,b=['{x:',t.x,',y:',t.y];
					if(t.label)b.push(',label:',t.label);
					b.push('}');
					return b.join('');
				}
				,appendChild:function(c){
					let t=c.parent=this;
					c.prv=t.lastChild;
					if(c.prv)t.lastChild.nxt=c;
					else t.firstChild=c;
					return t.lastChild=c;}
				,removeChild:function(c){
					if(!c||c.parent!=this)return;
					let t=this;
					if(t.firstChild==c)t.firstChild=c.nxt;
					if(t.lastChild==c)t.lastChild=c.prv;
					if(c.prv)c.prv.nxt=c.nxt;
					if(c.nxt)c.nxt.prv=c.prv;
					c.prv=c.nxt=c.parent=null;}
			}//Construct1stProto
		}//protoConstruction
		,initPage:window.onload=function () {
			let b=document.body
				,am=amos.man
				,c=am.context
				,cnvs,info={}
				,cd=c.dom
				,d=am.dom
				,st=d.e('style');
			st.innerHTML='fieldset{display:inline}';
			b.appendChild(st)
			amos.Construct.prototype=amos.man.protoConstruction.Construct1stProto;
			Object.keys(cd).forEach(k=>cd[k]=k=='canvas'?
				d.e(k,b,k):
				d.fldst(k,info[k]=d.e('span'),b)
			);cd.info=info;
			let a=amos.outline,x=new amos.
			Construct(amos,{x:50,y:50,label:{str:'ok'}});
			a.push(x); // amos.outline=[x];
			(cnvs=cd.canvas).onresize=c.events.onResz;
			c.ctx=cnvs .getContext('2d')
			cnvs.onmousemove=c.events.onMousMv
			cnvs.onmouseup=c.events.onMousUp
			cnvs.onmousedown=c.events.onMousDn;
			c.events.onResz();
			c.events.onDraw(c.ctx); //c.events.intrvl=setInterval(c.events.onDraw,100)
		}//initPage
		,context:{ctx:0,dom:{canvas:0,opDiv:0,amosDiv:0}
			,events:{m:{x:0,y:0}
				,down:{p:0//p::=ref to closest construct
					,m:{x:0,y:0}// m::=mouse-coords when mouse-down
					,pc:{x:0,y:0}// pc::= point-coords of closest when mouse-down
					// flag::= bool if mouse-button is currently down
					,flag:0}
				,closest:{p:0,dist:0
					,find:function(){
						let t=amos.man.context.events,c=t.closest,o=c.p //,oz=c.dist;
						c.dist=Number.MAX_VALUE;
						amos.outline.forEach(p=>{
							let z=p.distance(t.m);if(z<c.dist){
								c.dist=z;c.p=p;
							}});//if(o!=c.p)console.log('closest=',c.p,',old=',o);
						return c.p;}
				} //closest
				,onResz:function(evt){
					let b=document.body
						,am=amos.man
						,c=am.context
						,cd=c.dom
						,d=am.dom
						, p=cd.canvas
					,q=c.dim={w:p.width =document.body.clientWidth
						,h:p.height = document.body.clientHeight}
					q.cx=q.w/2;q.cy=q.h/2;
				}
				,onDraw:evt=>{
					let d=amos.man.context,ctx=d.ctx;
					ctx.fillStyle = '#fff';
					ctx.clearRect(0,0,d.dim.w,d.dim.h);
					ctx.strokeStyle = ctx.fillStyle = '#000';
					amos.outline.forEach(c=>c.onDraw(evt,ctx))
				}
				,onMousMv:function(evt){
					let t=amos.man.context.events;
					if(!evt)evt=event;
					t.m.x=evt.x //clientX;
					t.m.y=evt.offsetY // y // clientY;
					if(t.down.flag ) {
						//let m=t.m;//,b=t.down.p.boundingbox();
						t.onDrag(evt)
					}else//if(!t.down.flag)
					{
						let c=t.closest,z=c.o
						c.find();
						if(c.p!=z) {
							//console.log('closest:', c.p)
							if(z)
								z.onExit(evt)
							c.o=0;
							if(c.p.isOvr(t.m))
								(c.o=c.p).onOvr(evt);
							t.onDraw(evt)
						}else if(c.o )
						{if(c.o.isOvr(t.m))
							c.p.onMv(evt);
						else{
							c.o.onExit(evt);
							c.o=0;
							t.onDraw(evt)
						}
						}
					}
				}
				,onDrag:function(evt){
					let t=amos.man.context.events
					,d=t.down,p=d.p ||{x:0,y:0}
					,a={x:t.m.x-d.m.x,y:t.m.y-d.m.y},
					e={evt:evt,old:{x:p.x,y:p.y},delta:a };
					e.p={x://p.x=
							a.x+d.pc.x
						,y://p.y=
							a.y+d.pc.y}
					if(p&&p.setProp)
					{p.setProp('x',e.p.x);p.setProp('y',e.p.y);}
					p.onDrag(e);
					t.startAnim()//onDraw(evt)
				}
				,onMousUp:function(evt){
					let c=amos.man.context,t=c.events,d=t.down;
					d.flag=0;
					if(d.p){d.p.onBlur(evt);
					t.stopAnim()
					}}
				,onMousDn:function(evt){
					let am=amos.man
						,t=am.context.events
						,d=t.down,m=t.m
						,p=t.closest.o;//,b=p&&p.boundingbox();
					if(	p&&p.isOvr(m)){
					d.m.x=m.x;d.m.y=m.y;//d.m.x=evt.clientX;d.m.y=evt.clientY
					d.p=p;d.pc.x=p.x;d.pc.y=p.y;//d.bb=b;
					p.onFocus(d.flag=evt);
						amos.man.context.dom.info.opDiv.appendChild(p.meta.dom.propsDiv)
					}}
				,startAnim:z=>{
					let t=amos.man.context.events;if(!t.intrvl)
						t.intrvl=setInterval(t.onDraw,100);
					}
				,stopAnim:z=>{
					let t=amos.man.context.events;if(t.intrvl)
					{clearInterval(t.intrvl);t.intrvl=0;}
				}
			}//events
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
			,fldst:(lgnd,c,p,id,atrbs,styl)=>{
				let f=amos.man.dom.e('fieldset',0,id,atrbs,styl)
				,g=amos.man.dom.e('legend',f,0,0,0,lgnd)
				if(c)f.appendChild(c);
				if(p)p.appendChild(f);return f;}
			,bld:(params,parent)=>{
				/**BuildDomTree params::= id
		,n:nodeName
		,t:text (optional)
		,clk:onclick-function (optional)
		,chng:onchange-function (optional)
		,a:jsobj-attributes (optional)
		,c:jsarray-children-recursive-params (optional)
		,s:jsobj-style (optional)
		;
		or params canbe string, or array:call bldTbl
		;
		parent: domElement
				 */
				let t=amos.man.dom
				try{if(typeof(params)=='string')return t.txt(params,parent);
					let p=params,n=p.n?document.createElement(p.n):p.t!=undefined?document.createTextNode(p.t):p;
					if(p.n&&(p.t!=undefined))n.appendChild(document.createTextNode(p.t));
					if(p.n&&p.n.toLowerCase()=='input')n.type=(p.a?p.a.type:0)||'text';
					if(p.id)n.id=p.id;
					if(p.name||(p.a&&p.a.name))n.name=p.name||p.a.name;
					if(p.clk)n.onclick=p.clk;
					if(p.chng)n.onchange=p.chng;
					if(p.s)for(var i in p.s)n.style[i]=p.s[i];
					if(p.a)for(var i in p.a)n[i]=p.a[i];
					if(p.c){if(p.n&&p.n.toLowerCase()=='select')t.bldSlct(p.c,n,p);
					else if(p.n&&p.n.toLowerCase()=='table')t.bldTbl (p,n);
					else for(var i=0;i<p.c.length;i++)
							if(typeof(p.c[i])=='string')//t.dct(p.c[i],n);
								n.appendChild(document.createTextNode(p.c[i]));
							else
								t.bld(p.c[i],n);
					}
					//if(p.clpsbl)n=t.createCollapsable(p.clpsbl,parent,p.id,n);else
					if(parent)parent.appendChild(n);
				}catch(ex){console.error('amos.man.dom.bld:ex',ex);}return n;
			}//bld

			,bldTbl:function App_BuildHtmlTable(params,pr){
				/** params: same as bld.params
		.columnsHidden integer count of the first columns that should not generate html
		, .c  2 dimensional array of elements ,this method will nest each elem in a TD
			, and the elem is same as bld.params,recursive
		, .headings 1dim array , this method nests each elem in a TH, and the elem is same as bld.params
		, .footer
	, pr:parent dom-element , based-on/uses dce
				 */
				let t=amos.man.dom, tbl=pr&&pr.nodeName=='TABLE'?pr:t.dce('table',pr,null,params?params.id:0),tb=tbl?tbl.tBodies[0]:0;
				if(params.headings){var ht=tbl.tHead,a=params.headings,tr,th;if(!ht)ht=tbl.createTHead();tr=th.children?th.children[0]:0;if(!tr)tr=ht.insertRow();for(var i in a)if(!(params.columnsHidden>=i)){th=dce('th',tr);bld(a[i],th);}}
				if(params.c){var r,a=params.c,tr,td;for(var i in a){tr=tb.insertRow();tr.pk=a[i][j];if(a[i])for(var j in a[i])if(!(params.columnsHidden>=i)){td=tr.insertCell();bld(a[i][j],td);}}}
				if(params.footer){var ht=tbl.tFoot,a=params.footer,tr,td;if(!ht)ht=tbl.createTFoot();tr=ht.children?ht.children[0]:0;if(!tr)tr=ht.insertRow();for(var i in a)if(!(params.columnsHidden>=i)){td=tr.insertCell();bld(a[i],td);}}
				return tbl;}

			,bldSlct:function App_BuildSelect(a,pr,params){
				var c=params.c,i,n,t,v,s=params.selected||params.a.selected;
				for(i=0;c&&i<c.length;i++){pr.options[pr.options.length]=n=typeof(c[i])=='string'
				||!((c[i] instanceof Array&&c[i].length>1)||c[i].value)?new Option(t=v=c[i])
					:new Option(t=(c[i].text||c[i][0]),v=(c[i].value||c[i][1]));if(s==t||s==v)n.selected=true;}}

			,bldDataGrid:function App_BuildDataGrid(prm,pr){
				/**prm::=
		o ::= json obj that has the props , two way binding
		f ::= field meta-data: <key:fieldname>:{type:<str:number,integer,date,enum,bool> , max:<number for num/integer> ,min, step ,  }
	pr: parent dom-elemt
				 */
			}
		}//dom
	}//man
	,outline:[]
	,Construct:function(parent,params){
		// currently 1st-implementation (Point) //CompositeConstruct
		let t=this,p=params;
		t.x=p.x;t.y=p.y;t.label=p.label;//label.str,.ox,.oy
		if(parent&&parent.appendChild)
			parent.appendChild(t);
		t.initFields();
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
		width , strokeLineWidth
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
	ternary
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

** Applications
	* translator: codeJson to source-code( javascript ,python ,sql ,php ,java , mbj )
	* fsm (turing-machine io-tapes ,http-server )
		* bnf-parser(production rules): source-code parser to metaGraphics
 			(json,javascript
 			,python
 			, fs / mime
			,sql(ddl,ql)
			,xml,html,svg,css
			,regex
 			,java,jsp
			,mohBinaryJson ,mohBinaryXml , mxp ,flipzero(rrnp)
			,c ( .h , c++ , objective-c )
 			,php,amos,prolog,lisp,smalltalk,fortran,pascal,vrml,rockstar
 			,tsv,csv,xlsx,visio
			,font
			,3d(dae,obj,dwg,fbs)
			,imgs(raw,gif,png,jpg,iff,photoshop,pdf)
			,audio(iff,wav,mp3,ogg,midi)
			,vision(qrcode,openCV)  )
	* (visualizations)interpreter (stackframe debuger, tracing)
	* ER Diagram , scaffolding , control-flow-processes(document-cycle forms+emps)
	* events-visualizations (network visualizations (stations))
	* morph-class-browser / templates
	* ProjManSys + tasks , timeMan/DynaTimTbl +location-based-services
	* Graphics/art design (colors,themes, zones, focus , beauty/virtue/value-factors , analogy/resemblance ) , mindmap , calligraphy
	* Music Composing (sequences , patternBlocks , instr/samples , meta-melody patterns , effects(volume,slide,ect)
		, database , db:behaviour+categorising+impressionDegree&Axises )
	* (visualizations)software architecture (system-layers / dependencies)
	* (visualizations)RBAC
	* (visualizations)API (according to platform)
	* desmos app visualizations (maths + statistics )
		* (visualizations) Physics-Engine
	* pplQueue , simula-like , booking/allocation-sys
	* Speech Input Driven (knowledge/context based ai ,NLP ) , rakter , programmer
	* 3d encyclopedia reproducing (afterwards, recognition)
	* 3d robot body(physics) simu & planning(
		* sensors man
		* tob-bottom tasks breakdown(sequencing&simultanous tasks)
			, directives/priorities
			,resource allocation/utilization )
		* map-routing , map-construction/updating/digitizing

 *engines
 * browser-based
 * nodejs
 * nscript (c-base)
 * python-based
 * jvm js
 * php

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

<breakdown stage3>
	 Construct ,
	 1st-implementation is a point
	 2nd implementation is a group
	 3rd: implementation-add outline and path
	 4th: implementation-add borderOps
	 5th: implementation-add array-grid LayoutOnCanvas
 6th: implementation-add obj LayoutOnCanvas

	 */


/**<breakdown stage4>
 Construct ,
 1st-implementation is a point
	 1.0 circle (codebase , circle drawBorder)
	 1.1 events (onover,onexit)
	 1.2 props( dom-based introspection)
	 1.2 actions( dom-based buttons)
	 1.3 box
	 1.4 CoordSys visualizations(x-axis , y-axis, ticks(major,minor,labels) )
	 1.5 Polar CoordSys visualizations
 2nd implementation is a group
	 2.0 children
	 2.1 transform (wrt parent)
	 2.3 children visualizations
 3rd: implementation-add outline and path
	 3.0 glyph (path:points,curvs)
	 3.1 strokes styles
	 3.2 fill styles
	 3.1 glyphs
	 3.3 shapes (rect ,arc ,ellipse ,template )

 4th: implementation-add borderOps
	 4.0 metaGrfcal children
	 4.1 metaGrfcal border chidren
	 4.2 metaGrfcal btns chidren
	 4.3 metaGrfcal props (primitives&ref)chidren
	 4.3 metaGrfcal props chidren
	 4.4 metaGrfcal CoordSys chidren
	 4.5 metaGrfcal chidren visualization
	 4.6 metaGrfcal proto-ref & proto-props visualization
 5th: implementation-add array-grid LayoutOnCanvas
 6th: implementation-add obj LayoutOnCanvas
	 */

} // amos
