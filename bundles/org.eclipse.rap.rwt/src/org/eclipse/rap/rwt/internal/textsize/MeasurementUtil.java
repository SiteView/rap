/*******************************************************************************
 * Copyright (c) 2011, 2012 Frank Appel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Frank Appel - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.textsize;

import org.eclipse.rap.rwt.internal.protocol.ProtocolMessageWriter;
import org.eclipse.rap.rwt.internal.protocol.ProtocolUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.internal.graphics.FontUtil;


public class MeasurementUtil {

  public static void appendStartupTextSizeProbe( ProtocolMessageWriter writer ) {
    MeasurementOperator.getInstance().appendStartupTextSizeProbe( writer );
  }

  static Object createItemParamObject( MeasurementItem item ) {
    Object[] result = new Object[ 8 ];
    FontData fontData = item.getFontData();
    result[ 0 ] = getId( item );
    result[ 1 ] = item.getTextToMeasure();
    result[ 2 ] = ProtocolUtil.parseFontName( fontData.getName() );
    result[ 3 ] = Integer.valueOf( fontData.getHeight() );
    result[ 4 ] = Boolean.valueOf( ( fontData.getStyle() & SWT.BOLD ) != 0 );
    result[ 5 ] = Boolean.valueOf( ( fontData.getStyle() & SWT.ITALIC ) != 0 );
    result[ 6 ] = Integer.valueOf( item.getWrapWidth() );
    result[ 7 ] = Boolean.valueOf( isMarkup( item.getMode() ) );
    return result;
  }

  static Object createProbeParamObject( Probe probe ) {
    Object[] result = new Object[ 8 ];
    FontData fontData = probe.getFontData();
    result[ 0 ] = getId( probe );
    result[ 1 ] = probe.getText();
    result[ 2 ] = ProtocolUtil.parseFontName( fontData.getName() );
    result[ 3 ] = Integer.valueOf( fontData.getHeight() );
    result[ 4 ] = Boolean.valueOf( ( fontData.getStyle() & SWT.BOLD ) != 0 );
    result[ 5 ] = Boolean.valueOf( ( fontData.getStyle() & SWT.ITALIC ) != 0 );
    result[ 6 ] = Integer.valueOf( -1 );
    result[ 7 ] = Boolean.TRUE;
    return result;
  }

  static void addItemToMeasure( String toMeasure, Font font, int wrapWidth, int mode ) {
    FontData fontData = FontUtil.getData( font );
    MeasurementItem newItem = new MeasurementItem( toMeasure, fontData, wrapWidth, mode );
    MeasurementOperator.getInstance().addItemToMeasure( newItem );
  }

  static String getId( Probe probe ) {
    return getId( probe.getFontData() );
  }

  static String getId( MeasurementItem item ) {
    return "t" + Integer.toString( item.hashCode() );
  }

  static String getId( FontData fontData ) {
    return "p" + Integer.toString( fontData.hashCode() );
  }

  private static boolean isMarkup( int mode ) {
    return mode == TextSizeUtil.MARKUP_EXTENT;
  }

  private MeasurementUtil() {
    // prevent instance creation
  }
}