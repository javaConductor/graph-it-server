package com.graphomatic.security

/**
 * Created by lcollins on 11/9/2015.
 */
class AuthResult {

	boolean  mustChangePasword
	String authToken
	boolean  authenticated(){
		!!authToken
	}

}
