package com.notnoop.apns;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import com.notnoop.apns.internal.Utilities;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class BinaryInterfaceApnsNotification implements ApnsNotification {

	private final static byte COMMAND = 2;
    private static AtomicInteger nextId = new AtomicInteger(0);
    private final int identifier;
    private final int expiry;
    private final byte[] deviceToken;
    private final byte[] payload;
    private final int priority;
    
    public static int INCREMENT_ID() {
        return nextId.incrementAndGet();
    }
    
    /**
     * The infinite future for the purposes of Apple expiry date
     */
    public final static int MAXIMUM_EXPIRY = Integer.MAX_VALUE;

    /**
     * Constructs an instance of {@code ApnsNotification}.
     *
     * The message encodes the payload with a {@code UTF-8} encoding.
     *
     * @param dtoken    The Hex of the device token of the destination phone
     * @param payload   The payload message to be sent
     */
    public BinaryInterfaceApnsNotification(
            int identifier, int expiryTime, int priority,
            String dtoken, String payload) {
        this.identifier = identifier;
        this.expiry = expiryTime;
        this.deviceToken = Utilities.decodeHex(dtoken);
        this.payload = Utilities.toUTF8Bytes(payload);
        this.priority = priority;
    }

    /**
     * Constructs an instance of {@code ApnsNotification}.
     *
     * @param dtoken    The binary representation of the destination device token
     * @param payload   The binary representation of the payload to be sent
     */
    public BinaryInterfaceApnsNotification(
            int identifier, int expiryTime,int priority,
            byte[] dtoken, byte[] payload) {
        this.identifier = identifier;
        this.expiry = expiryTime;
        this.priority = priority;
        this.deviceToken = Utilities.copyOf(dtoken);
        this.payload = Utilities.copyOf(payload);
    }
    
	@Override
	public byte[] getDeviceToken() {
		return Utilities.copyOf(deviceToken);
	}

	@Override
	public byte[] getPayload() {
		 return Utilities.copyOf(payload);
	}

	@Override
	public int getIdentifier() {
		return identifier;
	}

	@Override
	public int getExpiry() {
		return expiry;
	}

	private byte[] marshall = null;
	@Override
	public byte[] marshall() {
		if (marshall == null) {
            marshall = Utilities.marshallBinary(COMMAND, identifier,
                    expiry, priority, deviceToken, payload, this.length());
        }
        return marshall.clone();
	}
	
    /**
     * Returns the length of the message in bytes as it is encoded on the wire.
     *
     * Apple require the message to be of length 255 bytes or less.
     *
     * @return length of encoded message in bytes
     */
    public int length() {
    	int lenIdentifier = identifier == -1 ? 0 : 4 + 3;
    	int lenExpires = this.expiry == -1 ? 0 : 4 + 3;
    	int lenPriority = this.priority == -1 ? 0 : 1 + 3;
    	int lenCommand = 1;
    	int lenLength = 4;
    	int lenToken = deviceToken.length + 3;
    	int lenPayload = payload.length + 3;
    	
        int length = lenCommand + lenLength + lenToken + lenPayload + lenIdentifier + lenExpires + lenPriority;
        return length;
    }

    @Override
    public int hashCode() {
        return (21
               + 31 * identifier
               + 31 * expiry
               + 31 * priority
               + 31 * Arrays.hashCode(deviceToken)
               + 31 * Arrays.hashCode(payload));
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BinaryInterfaceApnsNotification))
            return false;
        BinaryInterfaceApnsNotification o = (BinaryInterfaceApnsNotification)obj;
        return (identifier == o.identifier
                && expiry == o.expiry
                && priority == o.priority
                && Arrays.equals(this.deviceToken, o.deviceToken)
                && Arrays.equals(this.payload, o.payload));
    }

    @Override
    @SuppressFBWarnings("DE_MIGHT_IGNORE")
    public String toString() {
        String payloadString;
        try {
            payloadString = new String(payload, "UTF-8");
        } catch (Exception ex) {
            payloadString = "???";
        }
        return "Message(Id="+identifier+"; Token="+Utilities.encodeHex(deviceToken)+"; Payload="+payloadString+")";
    }

}
