package org.openmrs.module.pacsintegration.listener;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: abarbos
 * Date: 9/28/12
 * Time: 7:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class PatientMessage implements MapMessage {

    private Map<String, String> map = new HashMap<String, String>();

    @Override
    public boolean getBoolean(String s) throws JMSException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public byte getByte(String s) throws JMSException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public short getShort(String s) throws JMSException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public char getChar(String s) throws JMSException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getInt(String s) throws JMSException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public long getLong(String s) throws JMSException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public float getFloat(String s) throws JMSException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public double getDouble(String s) throws JMSException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getString(String s) throws JMSException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public byte[] getBytes(String s) throws JMSException {
        return new byte[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Object getObject(String s) throws JMSException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Enumeration getMapNames() throws JMSException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setBoolean(String s, boolean b) throws JMSException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setByte(String s, byte b) throws JMSException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setShort(String s, short i) throws JMSException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setChar(String s, char c) throws JMSException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setInt(String s, int i) throws JMSException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setLong(String s, long l) throws JMSException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setFloat(String s, float v) throws JMSException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setDouble(String s, double v) throws JMSException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setString(String s, String s1) throws JMSException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setBytes(String s, byte[] bytes) throws JMSException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setBytes(String s, byte[] bytes, int i, int i1) throws JMSException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setObject(String s, Object o) throws JMSException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean itemExists(String s) throws JMSException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getJMSMessageID() throws JMSException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setJMSMessageID(String s) throws JMSException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public long getJMSTimestamp() throws JMSException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setJMSTimestamp(long l) throws JMSException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public byte[] getJMSCorrelationIDAsBytes() throws JMSException {
        return new byte[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setJMSCorrelationIDAsBytes(byte[] bytes) throws JMSException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setJMSCorrelationID(String s) throws JMSException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getJMSCorrelationID() throws JMSException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Destination getJMSReplyTo() throws JMSException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setJMSReplyTo(Destination destination) throws JMSException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Destination getJMSDestination() throws JMSException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setJMSDestination(Destination destination) throws JMSException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getJMSDeliveryMode() throws JMSException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setJMSDeliveryMode(int i) throws JMSException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean getJMSRedelivered() throws JMSException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setJMSRedelivered(boolean b) throws JMSException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getJMSType() throws JMSException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setJMSType(String s) throws JMSException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public long getJMSExpiration() throws JMSException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setJMSExpiration(long l) throws JMSException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getJMSPriority() throws JMSException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setJMSPriority(int i) throws JMSException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void clearProperties() throws JMSException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean propertyExists(String s) throws JMSException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean getBooleanProperty(String s) throws JMSException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public byte getByteProperty(String s) throws JMSException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public short getShortProperty(String s) throws JMSException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getIntProperty(String s) throws JMSException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public long getLongProperty(String s) throws JMSException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public float getFloatProperty(String s) throws JMSException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public double getDoubleProperty(String s) throws JMSException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getStringProperty(String s) throws JMSException {
        return map.get(s);
    }

    @Override
    public Object getObjectProperty(String s) throws JMSException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Enumeration getPropertyNames() throws JMSException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setBooleanProperty(String s, boolean b) throws JMSException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setByteProperty(String s, byte b) throws JMSException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setShortProperty(String s, short i) throws JMSException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setIntProperty(String s, int i) throws JMSException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setLongProperty(String s, long l) throws JMSException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setFloatProperty(String s, float v) throws JMSException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setDoubleProperty(String s, double v) throws JMSException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setStringProperty(String s, String s1) throws JMSException {
        map.put(s, s1);
    }

    @Override
    public void setObjectProperty(String s, Object o) throws JMSException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void acknowledge() throws JMSException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void clearBody() throws JMSException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
