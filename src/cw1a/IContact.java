package cw1a; // or whatever

/**
 *
 * @author D Lightfoot 2025-01
 * DO NOT CHANGE THIS INTERFACE

 * objects of a class implementing this interface hold information
 * about a contact
 */
 
public interface IContact {
 
    /**
     * @pre true
     * @return the name of the contact
     */
    public String getName();
    
    /**
     * @pre true
     * @return the contact's affiliation
     */
    public String getAffiliation();
    
    @Override
    /**
     * @return the contact's name and affiliation as string
     */
    public String toString();
}

