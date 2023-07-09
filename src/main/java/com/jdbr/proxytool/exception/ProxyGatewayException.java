package com.jdbr.proxytool.exception;

/**
 * proxy gateway exception
 */
public class ProxyGatewayException extends RuntimeException{
  public ProxyGatewayException(String message){
      super(message);
  }
  public ProxyGatewayException(String message,Throwable cause){
      super(message,cause);
  }
}
