package holders;

/**
 * Class representing text file variable
 */
public class TFVAR {
	
	private final String name;
	private String value;
	private boolean valid;
	
	/**
	 * Text File Variable Holder<br>
	 * 
	 * Inside value stored in plain text, so can hold other variables.
	 * 
	 * @param n = (String) Name of variable in config file.
	 * @param v = (String) Value retrieved from config file.
	 */
	public TFVAR(String n, String v) {
		this.name = n; this.setValue(v);
	}

	/** Returns variable name. */
	public String getName() {
		return name;
	}

	/** Returns variable value. */
	public String getValue() {
		return value;
	}
	
	/** Returns variable value, when requested as String */
	@Override
	public String toString() {
		return value;
	}
	
	/** Set variable inside value.<br>
	 * Returns variable Object itself.
	 * @param v = (String) Value to set.
	 */
	public TFVAR setValue(String v) {
		this.value = v;
		this.setValid((this.value != null) && (this.value.length() > 0));
		return this;
	}

	/** Flag to validate and confirm stored value. */
	public boolean isValid() {
		return valid;
	}
	
	/** Set vairable vaulue validation flag.<br>
	 * Returns variable Object itself.
	 * @param isValid = (boolean) flag to set,
	 * depend of status of kept variable value.
	 */
	public TFVAR setValid(boolean isValid) {
		this.valid = isValid;
		return this;
	}
}
