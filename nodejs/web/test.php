<?php
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST");
header("Access-Control-Max-Age: 3600");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");
session_start() ;
 // https://www.techiediaries.com/php-rest-api/
/*
I've been a paying customer for webstorm for a number of years, and found out theres more features in ultimate, I thought webstorm was the complete package.

I would like to ask is intelliJ ultimate the complete package ?
and another question is can i upgrade my license/purchase of webstorm to intelliJ ultimate?

*/
class DBClass {

	private $host = "localhost" ;
	private $username = "royalfac_mysql";
	private $password = "Amiga1234";
	private $database = "royalfac_dept";

public $a=array( "dept"    ,"usr"    ,"mmbr"    ,"msg"    );

	public $connection;

	// get the database connection
	public function getConnection(){
		$this->connection = null;
		try{
			$this->connection = new PDO("mysql:host=" . $this->host . ";dbname=" . $this->database, $this->username, $this->password);
			$this->connection->exec("set names utf8");
		}catch(PDOException $exception){
			echo "Error: " . $exception->getMessage();
		}
		return $this->connection;
	}


	public function poll($lastPoll){
		$r=array();
		if(is_string($lastPoll))
			$lastPoll= (int) $lastPoll ; // number_format( str ) || intval (str ) || str +0 ;
		$x=$lastPoll /1000 ;
		foreach ($this->a as $tblNm ) {
			$sql="select * from `" . $tblNm . "` where `" .
				($tblNm=="msg"?"dt":"lastModified") . "` > ". $x;
			$r[$tblNm]=$this->connection.query($sql)->fetchAll();
		}
		return $r;
	}
}
///////////////////////////////////////////////////////////
class Base{

	// Connection instance
	public $connection;

	public function __construct($connection){
		$this->connection = $connection;
	}

	// table name
	public function table_name (){return "-";}
	public function columns(){return "$ cols" ;}

	public set($json){
		$a=this->columns();
		foreach($a as $c)
			$this[$c]=$json[$c];
	}


	//C
	public function create(){
		$sql = "insert into `".$this->table_name() ." values(:".join(",:",$this->columns().");";
		$stmt = $this->connection->prepare($sql);
		$stmt->execute($this);
		$r=$stmt->rowCount()
		return $r;
	}

	//R : read from database table
    public function read(){
        $pk=$this->columns[0];
		$query ="select * from `".$this->table_name() ."` where `".$pk."`=:".$pk;
		$stmt = $this->connection->prepare($query);
		$stmt->execute($this);
		$this->set($stmt->fetchAll());
		return $this;
    }

	//U
	public function update(){
		$c=$this->columns();
        $pk=$c[0];
		$sql = "update `".$this->table_name() ." set ";
		$i=0;
		foreach($c as $k){
			if($i >1)
				$sql =$sql." ,";
			if($i >0)
				$sql =$sql." `".$k."`=:".$k;
			$i ++ ;}
		$sql =$sql." where `".$pk."`=:".$pk;
		$stmt = $this->connection->prepare($sql);
		$stmt->execute($this);
		$r=$stmt->rowCount()
		return $r;
	}
	//D
	public function delete(){return 0;}
}
///////////////////////////////////////////////////////////
class Dept extends Base { // Family

public static final $cols=array("iddept","name","json","lastModified","parent");
public static final $tblNm = "dept"; // Family
	public function table_name (){return Dept::$tblNm;}
	public function columns(){return Dept::$cols;}

	/* table columns
CREATE TABLE `dept` (
  `iddept` int(11) NOT NULL UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  `name` varchar(255) NOT NULL,
  `json` longtext NOT NULL,
  `lastModified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `parent` int(11) DEFAULT NULL,
   KEY `index2` (`lastModified`),
   KEY `index3` (`name`),
   KEY `index4` (`parent`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ;
	*/

	public $iddept;
	public $name;
	public $json;
	public $lastModified;
	public $parent;

} // class Dept

///////////////////////////////////////////////////////////

class Usr extends Base  { // Product

public static final $cols=array("idusr" ,"name","json","lastModified");
public static final $tblNm = "usr";
	public function table_name (){return Usr::$tblNm;}
	public function columns(){return Usr::$cols ;}

	/* table columns
CREATE TABLE `usr` (
  `idusr` int(11) NOT NULL UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  `name` varchar(255) DEFAULT NULL,
  `json` longtext,
  `lastModified` timestamp NULL DEFAULT NULL,
   KEY `index2` (`name`),
   KEY `index3` (`lastModified`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

json (usrId , expire , email , tel)
usrId
access json {pw:<str:md5> , access:[]}
	*/
	public $idusr;
	public $name;
	public $json;
	public $lastModified;

}//class Usr

///////////////////////////////////////////////////////////

class Mmbr extends Base { // Transaction

    public static final $cols=array("idmmbr","level","json","deptId","usrId","lastModified");
	public static final $tblNm = "mmbr";
	public function table_name (){return Mmbr::$tblNm;}
	public function columns(){return Mmbr::$cols;}
	/* table columns
CREATE TABLE `mmbr` (
  `idmmbr` int(11) NOT NULL UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  `level` enum('disabled','readonly','owner','admin') NOT NULL DEFAULT 'readonly',
  `json` longtext NOT NULL,
  `deptId` int(11) NOT NULL,
  `usrId` int(11) NOT NULL,
  `lastModified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
   KEY `usrId_idx` (`usrId`),
   KEY `deptId_idx` (`deptId`),
   KEY `index4` (`lastModified`),
  `deptId` FOREIGN KEY (`deptId`) REFERENCES `dept` (`iddept`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  `usrId` FOREIGN KEY (`usrId`) REFERENCES `usr` (`idusr`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

	*/
	public $idmmbr;
	public $level;
	public $json;
	public $deptId;
	public $usrId;
	public $lastModified;

}//class Mmbr

///////////////////////////////////////////////////////////

class Msg extends Base { // Location

public static final $cols=array("idmsg" ,"owner","dt","dest","msg","dept","json");
public static final $tblNm = "msg";
	public function table_name (){return Msg::$tblNm;}
	public function columns(){return Msg::$cols ;}
	/* table columns

CREATE TABLE `msg` (
  `idmsg` int(11) NOT NULL UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  `owner` int(11) NOT NULL,
  `dt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `dest` int(11) NOT NULL,
  `msg` text NOT NULL,
  `dept` int(11) DEFAULT NULL,
  `json` longtext,
   KEY `index2` (`owner`,`dt`),
   KEY `index3` (`dt`),
   KEY `index4` (`dest`,`dt`),
  KEY `index5` (`dept`,`dt`),
  `fk1` FOREIGN KEY (`owner`) REFERENCES `usr` (`idusr`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  `fk2` FOREIGN KEY (`dest`) REFERENCES `usr` (`idusr`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  `fk3` FOREIGN KEY (`dept`) REFERENCES `dept` (`iddept`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

table msg(idMsg,owner fk(usr),dt,json,access)
table msgAttachment(msgId,json,access,payload longblob)
table msgDest(msgId , dest int fk(msg)
table log
	*/
	public $idmsg;
	public $owner;
	public $dt;
	public $dest;
	public $msg;
	public $dept;
	public $json;
} // class Msg

///////////////////////////////////////////////////////////
$r=null;
$mthd=$_SERVER['REQUEST_METHOD']
switch($mthd){

	case 'GET':{
		$lastPoll = $_GET["lastPoll"] ;
		if(is_numeric($lastPoll)){
			$dbclass = new DBClass();//echo json_encode($dbclass->a)
			$r=$dbclass->poll($lastPoll);}
	}break;

	case 'PUT':
	case 'POST':{
		$dbclass = new DBClass();
		$c=$dbclass->getConnection();
		$data = json_decode(file_get_contents("php://input"));
		$e = $data->e;
		$e = $e=="usr"?new Usr( $c ):$e=="dept"?new Dept( $c )
			:$e=="mmbr"?new Mmbr( $c ):$e=="msg"?new Msg( $c ):null;
		if($e!=null){
			$e->set($data);//$r=array();//$e->//$e->name = $data->name;//$e->lastModified = date('Y-m-d H:i:s');
			if($mthd=='PUT')$r=$e->update();
			else $r=$e->create()
		}echo
	}
 }// swich reqMethod
json_encode($r);
///////////////////////////////////////////////////////////

?>

