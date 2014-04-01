<?php
  //TODO: Check to see if the mysqli extension is loaded
  if (extension_loaded('mysqli'))
  {
  	//TODO: Check to make sure the user provided a POST argument
  	if ((isset($_POST["user"]))&&(isset($_POST["db"])))
  	{
  		//TODO: Sanitize the POST arguments before using them with the database
  		$user = mysqli::escape_string($_POST["user"]);
  		$db = mysqli::escape_string($_POST["db"]);
  		
  		//TODO: Log in to the database
  		if ($mysqli = new mysqli("localhost", $user, , $db))
  		{
  			
  		}
  	}
  	else
  	{
  		//TODO: If not, return HTTP error code 400 Bad Request
  		header('HTTP/1.0 400 Bad Request');
  	}
  }
  else
  {
  	//TODO: If not, return HTTP error code 500 Internal Server Error
  	header('HTTP/1.0 500 Internal Server Error');
  }
?>
