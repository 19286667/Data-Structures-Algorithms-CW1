package cw1a;
 
/**
 *
 * @author D Lightfoot 2025-01
 * 
 */
 
public class Contact implements IContact{   
    private final String name;
    private final String affiliation;
    
    public Contact(String name, String affiliation){
        this.name = name; this.affiliation = affiliation;
    }
    /**
     * Retrieves the name of the contact
     * @pre true
     * @return the name of the contact
     */
    public String getName(){return name;}
    
    /**
     * Retrieves the contact's affiliation
     * @pre true
     * @return the contact's affiliation
     */
    public String getAffiliation(){return affiliation;}
    
    @Override
    public String toString() {
        return this.getName() + ": " + this.getAffiliation();
    }
}
