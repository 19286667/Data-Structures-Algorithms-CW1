package cw1a;

public interface IContactDB {
    
/**
 *
 * @author D Lightfoot 2025-01
 * an objects of a class implementing this interface holds a
 * database of contact information
 
 * DO NOT CHANGE THIS INTERFACE
 *
 */ 
    public int getNumEntries();
    public int getTotalVisited();
    public void resetTotalVisited();

    /**
     * Empties the database.
     * @pre true
     */
    public void clearDB();
    
    /**
     * Determines whether a contact's name exists as a key inside the database
     * @pre name is not null and not empty string
     * @param name the contact name (key) to locate
     * @return true iff the name exists as a key in the database
     */
    public boolean containsName(String name);
        
    /**
     * Returns a Contact object mapped to the supplied name.
     * @pre name not null and not empty string
     * @param name The contact name (key) to locate
     * @return the Contact object mapped to the key name if the name
         exists as key in the database, otherwise null
     */
    public Contact get(String name);
    
    /**
     * @pre true
     * @return number of contacts in the database. 
     */
    public int size();
	
    /**
     * @pre true
     * @return true iff the database is empty
     */
    public boolean isEmpty();
    
    /**
     * Inserts an Contact object into the database, with the key of the supplied
     * contact's name.
     * Note: If the name already exists as a key, then then the original entry
     * is overwritten. 
     * This method must return the previous associated value 
     * if one exists, otherwise null
     * 
     * @pre contact not null and contact name not empty string
     */
    public Contact put(Contact contact);
    
    /**
     * Removes and returns a contact from the database, with the key
     * the supplied name.
     * @param name The name (key) to remove.
     * @pre name not null and name not empty string
     * @return the removed Contact object mapped to the name, or null if
     * the name does not exist.
     */
    public Contact remove(String name);
    
    /**
     * Prints the names and affiliations of all the contacts in the database in 
     * alphabetic order.
     * @pre true
     */
    public void displayDB();
}

