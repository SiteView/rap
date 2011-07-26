/*******************************************************************************
 * Copyright (c) 2011 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rwt.widgets;

import java.io.IOException;
import java.net.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.rwt.RWT;
import org.eclipse.rwt.service.IServiceHandler;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Synchronizer;

/**
 * Instances of this class provide synchronization support for displays in 
 * high-availability cluster environments. 
 * <p>
 * If transparent session failover in conjunction with <code>(a)syncExec()</code>
 * is required, then this synchronizer should be used. Call <code>setSynchronizer()</code> right 
 * after the display was constructed.
 * </p>
 * 
 * @see Synchronizer
 * @see Display#setSynchronizer
 * @see Display#syncExec(Runnable)
 * @see Display#asyncExec(Runnable)
 * @since 1.5
 */
public class ClusteredSynchronizer extends Synchronizer {
  private final String requestUrl;
  private final String cookies;
  
  public ClusteredSynchronizer( Display display ) {
    super( display );
    requestUrl = AsyncExecServiceHandler.createRequestUrl( RWT.getRequest() );
    cookies = extractRequestCookies( RWT.getRequest() );
    AsyncExecServiceHandler.register();
  }

  protected void runnableAdded( Runnable runnable ) {
    notifyAsyncExecServiceHandler();
  }

  static String extractRequestCookies( HttpServletRequest request ) {
    String result = "";
    Cookie[] requestCookies = request.getCookies();
    if( requestCookies != null ) {
      for( Cookie requestCookie : requestCookies ) {
        if( result.length() > 0 ) {
          result += "; ";
        }
        result += requestCookie.getName() + "=" + requestCookie.getValue();
      }
    }
    return result;
  }

  private void notifyAsyncExecServiceHandler() {
    try {
      sendAsyncExecServiceHandlerRequest();
    } catch( IOException ioe ) {
      throw new RuntimeException( ioe );
    }
  }

  private void sendAsyncExecServiceHandlerRequest() throws IOException {
    HttpURLConnection connection = createConnection();
    connection.connect();
    int responseCode = connection.getResponseCode();
    if( responseCode != HttpURLConnection.HTTP_OK ) {
      String msg = "AsyncExec service request returned response code " + responseCode;
      throw new IOException( msg );
    }
  }

  private HttpURLConnection createConnection() throws IOException {
    URL url = new URL( requestUrl );
    HttpURLConnection result = ( HttpURLConnection )url.openConnection();
    if( cookies.length() > 0 ) {
      result.setRequestProperty( "Cookie", cookies );
    }
    return result;
  }

  static class AsyncExecServiceHandler implements IServiceHandler {
    static final String ID = "asyncExecServiceHandler";

    static void register() {
      AsyncExecServiceHandler serviceHandler = new AsyncExecServiceHandler();
      RWT.getServiceManager().registerServiceHandler( ID, serviceHandler );
    }
    
    static String createRequestUrl( HttpServletRequest request ) {
      StringBuffer buffer = new StringBuffer();
      buffer.append( "http://127.0.0.1:" );
      buffer.append( request.getServerPort() );
      buffer.append( request.getRequestURI() );
      buffer.append( "?" );
      buffer.append( IServiceHandler.REQUEST_PARAM );
      buffer.append( "=" );
      buffer.append( ID );
      return buffer.toString();
    }
    
    public void service() {
      // do nothing
    }
  }
}