<?php
//client id and client secret
//please fill up the client & secret key below
$client = '';
$secret = '';
//hardcode the full url to redirect to this file
$url = "";

//STEP 1 - Get Access Code
if(!isset($_REQUEST['code']) && !isset($_REQUEST['access_token']))
{
	header( "Location: https://graph.api.smartthings.com/oauth/authorize?response_type=code&client_id=$client&redirect_uri=".$url."&scope=app" ) ;
}
//STEP 2 - Use Access Code to claim Access Token
else if(isset($_REQUEST['code']))
{
	$code = $_REQUEST['code'];
	$page = "https://graph.api.smartthings.com/oauth/token?grant_type=authorization_code&client_id=".$client."&client_secret=".$secret."&redirect_uri=".$url."&code=".$code."&scope=app";
	$ch = curl_init();
	curl_setopt($ch, CURLOPT_URL,            $page );
	curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1 );
	curl_setopt($ch, CURLOPT_POST,           0 );
	curl_setopt($ch, CURLOPT_HTTPHEADER,     array('Content-Type: application/json')); 
	$response =  json_decode(curl_exec($ch),true);
	curl_close($ch);
	if(isset($response['access_token']))
	{
		//Redirect to self with access token for step 3 for ease of bookmarking
		header( "Location: ?access_token=".$response['access_token'] ) ;
	}
	else
	{
		print "error requesting access token...";
		print_r($response);
	}
}
//Step 3 - Lookup Endpoint and write out urls
else if(isset($_REQUEST['access_token']))
{
	$url = "https://graph.api.smartthings.com/api/smartapps/endpoints/$client?access_token=".$_REQUEST['access_token'];
	$json = implode('', file($url));
	$theEndpoints = json_decode($json,true);
	print "<html><head><style>h3{margin-left:10px;}a:hover{background-color:#c4c4c4;} a{border:1px solid black; padding:5px; margin:5px;text-decoration:none;color:black;border-radius:5px;background-color:#dcdcdc}</style></head><body>";
	print "<i>Save the above URL (access_token) for future reference.</i>";
	print " <i>Right Click on buttons to copy link address.</i>";
	
	foreach($theEndpoints as $k => $v)
	{
//GET SWITCHES
		$switchUrl = "https://graph-eu01-euwest1.api.smartthings.com".$v['url']."/switches";
		$access_key = $_REQUEST['access_token'];
		$ch = curl_init($switchUrl);
		curl_setopt( $ch, CURLOPT_HTTPHEADER, array( 'Authorization: Bearer ' . $access_key ) );
		curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1 );
		curl_setopt($ch, CURLOPT_POST,           0 );
		$resp =  curl_exec($ch);
		curl_close($ch);
		$respData = json_decode($resp,true);
		
		if(count($respData) > 0) print "<h2>Switches</h2>";
		//let's show each of the switches
		foreach($respData as $i => $switch)
		{
			$label = $switch['label'] != "" ? $switch['label'] : "Unlabeled Switch";
			print " <h3>$label</h3>";
			$onUrl = "https://graph-eu01-euwest1.api.smartthings.com".$v['url']."/switches/".$switch['id']."/on?access_token=".$_REQUEST['access_token'];
			print "<a target='cmd' href='$onUrl'>On</a>";
			$offUrl = "https://graph-eu01-euwest1.api.smartthings.com".$v['url']."/switches/".$switch['id']."/off?access_token=".$_REQUEST['access_token'];
			print "<a  target='cmd' href='$offUrl' value='Off'>Off</a>";
			$toggleUrl = "https://graph-eu01-euwest1.api.smartthings.com".$v['url']."/switches/".$switch['id']."/toggle?access_token=".$_REQUEST['access_token'];
			print "<a target='cmd' href='$toggleUrl'>Toggle</a><BR>";
		}
		
//GET LOCKS
		$lockUrl = "https://graph-eu01-euwest1.api.smartthings.com".$v['url']."/locks";
		$access_key = $_REQUEST['access_token'];
		$ch = curl_init($lockUrl);
		curl_setopt( $ch, CURLOPT_HTTPHEADER, array( 'Authorization: Bearer ' . $access_key ) );
		curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1 );
		curl_setopt($ch, CURLOPT_POST,           0 );
		$resp =  curl_exec($ch);
		curl_close($ch);
		$respData = json_decode($resp,true);
		if(count($respData) > 0) print "<h2>Locks</h2>";
        //let's show the locks
		foreach($respData as $i => $lock)
		{
			$label = $lock['label'] != "" ? $lock['label'] : "Unlabeled Lock";
			print "<h3>$label</h3>";
			$lockUrl = "https://graphgraph-eu01-euwest1.api.smartthings.com".$v['url']."/locks/".$lock['id']."/lock?access_token=".$_REQUEST['access_token'];
			print "<a target='cmd' href='$lockUrl'>Lock</a>";
			$unlockUrl = "https://graph-eu01-euwest1.api.smartthings.com".$v['url']."/locks/".$lock['id']."/unlock?access_token=".$_REQUEST['access_token'];
			print "<a  target='cmd' href='$unlockUrl' value='Off'>Unlock</a><BR>";
		}
		print "<BR><hr><BR>";
	}
	//all links in the html document are targeted at this iframe
	print "<iframe name='cmd' style='display:none'></iframe></body></html>";
}
?>