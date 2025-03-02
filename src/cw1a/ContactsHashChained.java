package cw1a; //declares that this class belongs to that package. Packages are utilised in Java to organise related classes and ovoid naming conflicts.

import java.util.LinkedList; //importing the LinkedList from javas standard utility package, which will be used for the chaining mechanism in the hash table.

/**
 *
 * @author OJ 2025-02
 */

public class ContactsHashChained implements IContactDB {  //declares a public class named ContactsHashChained which implements the IContactDB interface.
    //the implements keyword means this class has to provide concrete implementations of all methods declared in the IContactDB interface. Interfaces in Java define a contract of methods that implementing classes must fulfill.
    //Class Variables / Fields
    private static final int[] PRIME_SIZES = { // An array of prime numbers used for table sizes. Using primes for HT sizes helps minimise collisions. 
        // static and final keywords mean this array is constant, i.e. doesn't change, and is shared across all instances of the class. 
        1009, 2003, 4001, 8009, 16001, 32003, 64007, 128021
    };
    private int primeIndex = 0; // keeps track of which prime in the array is currently being used as the table size. Initialised to the first index in the array, which is 0.
    private LinkedList<Contact>[] table; //this is the core data structure: an array where each element is a LinkedList of Contact objects, which implements the chaining approach to collision resolution in HTs.
                                         //when multiple keys hash to the same index, they're stored in a LinkedList at that location.
    private int tableCapacity; //stores the current capacity of the HT.
    private int numEntries; //tracks how many Contact objects are currently stored in the HT.
    private int totalVisited = 0; //Keeps count of how many buckets/positions have been accessed during operations, for analysing the efficiency of the HT.
    
    // ####OPEN ADDRESSING ITERATION CODE TEMPORARILY KEPT COMMENTED OUT FOR REFERENCE####
    /**Declare a static final object acting as sentinel for marking deleted entries in the hash table
     * This allows for null slots (never used), regular Contact objects (i.e. placed in 'active' slots),
     * and deleted slots (populated by the 'DELETED' sentinel objects) to be distinguished. 
     * As such, proper probe sequencing is maintained. 
     */
    //private static final Contact DELETED = new Contact("__DELETED__","0"); //commented out code from prior open addressing iteration where DELETED const was neccessary for lazy-deletion implementation. Kept here for now in case more extensive retroactive testing is to be carried out
    //########################################################################################
    
    // Set the maximum load factor prior to resizing
    private static final double maxLoadFactor = 50.0; //This defines when the table should be resized. LF is the ratio of entries to the table capacity, expressed as a percentage.
                                                      //Here, when the table reaches 50% full, it will trigger resizing to maintain efficiency.
    
//### OLD ITERATION CODE FOR REFERENCE WHILE REFACTORING ###        
    //public int getNumEntries(){return numEntries;}
    //public void resetTotalVisited() {totalVisited = 0;}
    //public int getTotalVisited() {return totalVisited;}
//#########################################################    

    
    /**
     * Constructor - hash table with chaining
     */ 
    public ContactsHashChained() { //this constructor initialises a new HT
        System.out.println("Hash Table with chaining"); //prints a message indicating the type of HT created in this iteration.
        this.tableCapacity = PRIME_SIZES[primeIndex]; //sets the initial capacity to the first prime number in the PRIME_SIZES array.
        table = new LinkedList[tableCapacity]; //creates a new array of LinkedList objects with that capacity. 
        clearDB(); // calls the clearDB method to initialise each LinkedList in the array.
    }

    // Accessors and Mutators (getters and setters)
    
    /**
     * getter for the number of entries held within the database
     * @return : number of entries
     */
    @Override //indicate this as an implementation of method declared in the IContactDB interface
    public int getNumEntries(){
        return numEntries; //returns the count of entries in the database.
    }
    
    /**
     * getter for the number of buckets visited over the course of operations
     * @return : the total number of visited buckets
     */
    @Override //indicate this is an implementation of method declared in the IContactDB interface.
    public int getTotalVisited(){
        return totalVisited; // returns the count of bucket accesses for performance analysis.
    }
    
    /**
     * setter for the counter denoting the total number of visited buckets
     * re-sets to 0
     */
    @Override // indicate this as implementation of IContactDB defined method
    public void resetTotalVisited(){
        totalVisited = 0; // resets the counter for bucket accesses back to 0.
    }

    
    
    /**
     * Empties the database, while preserving the markers for 'lasily DELETED' 
     * Contact objects in the appropriate slots. This faciliates maintenance of
     * probe sequence structure and, if built upon in future could help to
     * optimise future insertions.
     * Further, load factor calculations must account for prior deletions, so
     * tabs must be kept on deleted entries to accomodate their functionality.
     *
     * @pre true
     */
    @Override // indicate implementation of IContactDB defined method
    public void clearDB() { // Re-initialises the entire HT by...
        table = new LinkedList[tableCapacity]; // Creating a new array of LinkedList objects with the current capacity
        for (int i = 0; i < table.length; i++) { //(this for loop iterates through each position in the array and places a new, empty LL at that position. Without this, trying to access any LL in the table would yield a NullPointerException. 
            table[i] = new LinkedList<>(); //Initialising each chain, i.e. each element in the array with an empty LinkedList
        }
        numEntries = 0; // Resetting the counter for the number of entries. This line resets the counter that tracks how many Contact objects are stored in the HT to zero, since effectively all entries have been removed by creating a fresh HT.
    }
            
    /**
     * getter for the number of contacts 
     * @pre : true
     * @return : number of contacts in the database
     */
    @Override// indicate implementation of IContactDB method
    public int size(){
        return numEntries; // returns the number of entries in the DB.
    }
    
    /**
     * Determines wheather or not the database is empty
     * @pre : true
     * @return : true iff the database is empty
     */
    @Override //indicate implementation of IContactDB method.
    public boolean isEmpty(){
        return numEntries == 0; //returns true of there are no entries in the DB.
    }
    
    /**
     * Hash function for keys of type String.
     * @param : s, i.e. the String key to hash 
     * @return : the hash value of that string
     */

    private int hash(String s) { //converts a string key (contact name) into an integer index for the HT.
        assert  s != null && !s.trim().equals(""); //Assertion to ensure the string is not null or empty. The trim method removes whitespace from both ends of the string.
                                                   //This is defensive programming, preventing runtime errors like NullPointerException
        System.out.println("Hashing string: '" + s + "'"); //prints the string being hashed for output analysis/debugging.
        final int PRIME = 31; //defines the appropriately sized (small enough to minimise overflow risks, but large enough to provide good distribution) prime number constant used in the hash calculation. Used becauses primes help distribute has values more evenly. 
        int hash = s.length() * s.charAt(0);//Intitial hash val calculation: combine the string length with the first character. Multiplication provides better distribution than addition. Front-loads the hash computation with distinguishing characteristics.
        //implements polynomial hash function where each character contributes to the hash value, but the same characters in different positions affect the hash differently.
        for (int i = 1; i < s.length(); i++){ //loop through from 2nd character - the first has already been incorporated during front-loading...This looping ensures anagrams produce different hash values
            hash = (hash * PRIME + s.charAt(i)) % table.length; // the modulo at each step ensures the hash is kept within the HTs bounds, prevents int overflow furing calculation, and contributes to even distribution across the available range.
        }
        
        return Math.abs(hash);   //return the hash as an absolute value, ensuring non-negative and therefore within valid index range of the HT array.
        
    }

    /**
     * Calculates the value for the table's current load factor.
     * @return the present load factor (expressed as a percentage)
     */
    private double loadFactor() { //calculates the current lf of the ht as percentage. Used to determine when to resize the table, i.e. when lf exceeds maxLoadFactor 
        return (double) numEntries / (double) table.length * 100.0; //type casting to double ensures accurate results by floating point division
    } 
    
    // RECURSIVE IMPLEMENTATION OF containsName
    
    /**
     * Determines whether a contact's name exists as a key inside the database.
     * @pre name is not null and not empty string
     * @param name the contact name (key) to locate
     * @return true iff the name exists as a key in the database
     */
    @Override // Indicate implementation of interface defined method
    public boolean containsName(String name) {
        assert name != null && !name.equals(""); //ensures the name parameter is valid
        int pos = hash(name); // Uses the hash function to find the index position for this name. 
        LinkedList<Contact> chain = table[pos]; // retrieves the LinkedList at that position.
        totalVisited++; // Increments the counter for bucket accesses.
        return containsNameInChain(chain, name,0); // Calls the recursive helper method containsNameInChain to search for the name in the chain.
    }
    
    /**
     * Recursively check whether or not a name is present within a chain.
     * @param : chain, i.e: the LinkedList to search
     * @param : name, i.e: the name to be searched for
     * @param : index, i.e: the present position within the chain
     * @return true, if the name is found. 
     */
    private boolean containsNameInChain(LinkedList<Contact> chain, String name, int index){ // recursive helper method for containsName. Searches for a name in a chain LinkedList. Linear search through the chain using recursion rather than a typical loop.
        //for the base case the end of the list
        if (chain == null || index >= chain.size()){ //base case: name NOT found, i.e. if the chain is null or end of list reached: return false 
            return false;
        }
        Contact contact = chain.get(index); //fetches the Contact object at the current position to be checked against the sought name
        
        
        //for the base case that the contact is found
        if (name.equals(contact.getName())){ // base case: name FOUND, i.e. if the current contacts name matches the search name: return true.
            return true;
        }
        
        //the recursive case is to search the next chain element
        return containsNameInChain(chain, name, index+1); // Recursive case: call the same method with the next index position, i.e. index +1 
    }
    // RECURSIVE IMPLEMENTATION OF GET
    
    /**
     * Returns a contact object mapped to the supplied name.
     * @pre name not null and not empty string
     * @param name The contact name (key) to locate
     * @return the Contact object mapped to the key name if the name exists as key in the 
     * database, otherwise null
     */
    @Override //indicate implementation of interface defined method
    public Contact get(String name){
        assert name != null && !name.trim().equals(""); //ensures name param is valid
        int pos = hash(name); //uses hash funct to find the index position for this name.
        
        System.out.println("\nStarting search for: " + name); //debug print stmnt
        System.out.println("Initial hash position: " + pos); //debug print stmnt
        
        LinkedList<Contact> chain = table[pos]; //retrieves the LL at that position
        totalVisited++; //count visiting the bucket
        
        //calling the rcursive helper method. i.e. delegating the actual search to getFromChain
        return getFromChain(chain, name, 0);
    }
    
    /**
     * Recursive implementation of search for a contact within a chain.
     * @param : chain, i.e. the LinkedList to search
     * @param : name, i,e. the name to be sought
     * @param : index, i.e. the present position in the chain
     * @return the contact if found, otherwise null.
     */
    private Contact getFromChain(LinkedList<Contact> chain, String name, int index){
        //Base case : the end of the list
        if (chain == null || index >= chain.size()){ //base case: NOT found, i.e. chain is null or end of list reached: return null
            System.out.println("Position is empty or name not found");
            return null;
        }
        
        Contact contact = chain.get(index); //retrieves the current contact object from the LL for examination.
        System.out.println("Visiting chain element: " + contact.getName());
        totalVisited++; //increment counter to track each comparison within the chain
        
        //Base case : the sought contact has been found
        if (name.equals(contact.getName())){ //base case: FOUND, i.e. the current contacts name matches the search name: return the contact
            System.out.println("Found " + contact.getName());
            return contact;
        }
        
        //The recursive case being to check the next element in the chain
        return getFromChain(chain, name, index+1); //continue searching with the next index
    } 
    
    // RECURSIVE IMPLEMENTATION OF PUT
    
    /**
     * A helper method for put using recursion that does not trigger table resizing.
     * @param : contact, i.e. the Contact object to add
     * @return the previous Contact with the same name if it exists, otherwise null.
     */
    private Contact putWithoutResizing(Contact contact){
        String name = contact.getName(); // get contacts name
        int pos = hash(name); // get the contacts name
        LinkedList<Contact> chain = table[pos]; // compute that name's hash position
        totalVisited++; // increment the bucket access counter
        
        //check if the contact exists in the chain and replace if so, otherwise add
        //that contact to the chain.
        Contact previous = putInChain(chain, contact, 0); // calls the recursive helper method putInChain to add or replace the contact in the chain.
        
        //If the contact was newly added, rather than replaced, increment the counter.
        if (previous==null){ //if no previous contact with the same name existed
            numEntries++; //increment the entry counter
        }
        
        return previous; //returns the previous contact with the same name, if any. 
    }
    
    /**
     * Traverse a chain recursively to add, or replace, a contact.
     * @param : chain - the linkedList to be modified
     * @param : contact - the contact to add or replace
     * @param : index - the present position in the chain.
     * return previous contact if they've been replaced, if added: null
     */
    
    private Contact putInChain(LinkedList<Contact> chain, Contact contact, int index){ // Recursive helper method for Put
        String name = contact.getName();
        
        //Base case: end of the list. Add the new contact
        if (index >= chain.size()){
            chain.add(contact);
            return null;
        }
        
        Contact existing = chain.get(index); //retrieve contact at that index for examination
        totalVisited++; //increment counter for each contact visited during the search
        
        //Base case: matching name has been found. Replace contact.
        if (name.equals(existing.getName())){ //if a contact with a matching name has been found
            return chain.set(index, contact); //replace it with the new contact and return the old one. The set method returns the element that was previously at that position. By returning this value directly, the method passes the old contact back to the caller.
        }
        
        //Recursive case : proceed to check the next element in the chain
        return putInChain(chain, contact, index+1); 
    }
    
    @Override
    public Contact remove(String name){ //not implemented properly as specified in CW task defenition, but overriden since it's a method defined by the IContactDB interface.
        Contact contact = get(name);
        
        return contact;
    }
    
    /**
     * Inserts a Contact object into the database, with the key of the supplied
     * contact's name.
     * @pre contact not null and contact name not empty string
     * @param contact Contact to add
     * @return previous Contact with same name if exists, otherwise null
     */
    @Override //implemented method defined by interface
    public Contact put(Contact contact){ //public method to add contact to DB
        assert contact != null; //validate contact not null
        String name = contact.getName(); //obtain the existing contacts name
        assert name != null && !name.trim().equals(""); // validate the name is not null or empty
        
        Contact previous = putWithoutResizing(contact); //calls putWithoutResizing to add the contact to the appropriate chain
        
        if (previous == null && loadFactor() > maxLoadFactor){ //if a new contact was added - not replacing the existing one - and the lf exceeds the max, trigger resizing of HT.
            resizeTable();
        }
        
        return previous; //returns the previous contact with the same name, if any.
    }
    // displayDB IMPLEMENTATION WITH RECURSION
    
    /**
     * Prints the names and affiliations of all the contacts in the database in
     * alphabetic order.
     * @pre true
     */
    @Override public void displayDB(){
        System.out.println("capacity " + table.length + " size " + numEntries + " Load factor " + loadFactor() + "%");
        
        // Show hash table structure 
        for (int i = 0; i < table.length; i++) { //loop to iterate through every index (i) position, i.e. bucket, in the HT.
            LinkedList<Contact> chain = table[i]; //Retrieves the LL, the chain, at the current bucket position (i). This LL may contain multiple contacts if collisions occured at this has position.
            if (chain != null && !chain.isEmpty()){ //checks the chain exists and contains at least one contact. Only non-empty chains will be displayed, to keep output ordered and interpretable.
                System.out.print(i + ": "); // print the index: ...print used instead of println because the line will be continued with chain contents.
                displayChain(chain, 0); //calls the recursive helper method displayChain to display all contacts in this chain, starting at the first element in the chain.
                //this will print each contact followed by -> and ending in null to show the structure of the LinkedList/chain.
            }
        }
        
        //Display contacts in alphabetical order
        Contact[] toBeSortedTable = new Contact[numEntries]; //creates a new array of size exactly matching the number of entries/contacts, to hold all contacts
        int index = 0; //initialise counter to hold to keep track of the current position in the sorting array.
        
        // Fill array with all contacts
        for (int i=0; i < table.length; i++){ // starts another loop through all bucket positions in the HT. same range as the previous loop but for alphabetical purpose
            LinkedList<Contact> chain = table[i]; //again, retrieves chain at current bucket position to collect contacts for sorting.
            if (chain != null && !chain.isEmpty()){ //checks again if the chain exists and contains contacts, this time for sorting
                fillSortArray(chain, toBeSortedTable, index); //Calls the helper method to copy all contacts from the current chain into the sorting array. Params: chain, the LL to copy from; toBeSortedTable, the destination array; index, the starting position in the destination array.
                //Update index based on how many contacts were added
                index +=chain.size(); //updates the index position by adding the number of contacts in the current chain, ensuring the next chains contacts will be placed after the current ones in the array. 
            }
        }
        
        quicksort(toBeSortedTable, 0, numEntries -1);//Calls the QS algorithm to sort all contacts in the array by name. Params: the array to sort, the starting index of the array, and the ending index i.e. last element in the array
        
        System.out.println("\nContacts in alphabetical order:");
        for (int i = 0; i < numEntries; i++){ // starts loop to iterate through each element in the sorted array.
            System.out.println(i + " " + toBeSortedTable[i].toString()); //creates a numbered list of contacts in alphabetical order.
        }
    }
    
    /**
     * Recursively display all the contacts in a chain.
     * @param chain the LinkedList to display
     * @param index present position in the chain.
     */
    private void displayChain(LinkedList<Contact> chain, int index){ 
        //Base case: end of chain
        if (index >= chain.size()){
            System.out.println("null");
            return;
        }
        
        //Print current element
        System.out.print(chain.get(index).toString() + " -> ");
        
        //Recursive case: print the next element
        displayChain(chain, index +1);
    }
    
    /**
     *Fill an array with contacts from a chain.
     * @param chain the linked list from which to obtain the contacts
     * @param array the array to be filled
     * @param startIndex the starting position index of the array
 
     */
    private void fillSortArray(LinkedList<Contact> chain, Contact[] array, int startIndex){ //iterative helper method for displayDB
        for (int i=0; i < chain.size(); i++){ //standard loop to iterate through each element in the chain
            array[startIndex + i] = chain.get(i); //copy each contact from the chain to the array. The position in the destination array is offeset by startIndex to account for contacts already added from previous chains
        }
        
    }
    
    /**
     * Quicksort implementation for sorting contacts by name.
     * @param a Array of contacts to sort
     * @param low lower index
     * @param high Upper index
     */
    private void quicksort(Contact[] a, int low, int high){
        assert a != null && 0 <= low && low <= high && high < a.length; //verify valid params
        int i = low, j = high;
        Contact temp;
        if (high >= 0){
            String pivot = a[(low + high) / 2].getName(); //selects a pivot element from the beginning of the array
            while (i <= j){
                while (a[i].getName().compareTo(pivot) < 0) i++; //inc i until element found that is greater than or equal to the pivot
                while (a[j].getName().compareTo(pivot) > 0) j--; //dec j until element found that is less than or equal to the pivot
                if (i <= j){ //if i and j have not crossed, swap the elements, placing smaller ones before larger ones 
                    temp = a[i]; a[i] = a[j]; a[j] = temp;
                    i++; j--;
                }
            }   //recursive calls, dividing the array into smaller segments for sorting 
                if (low < j) quicksort(a, low, j); //sorts the left segment, i.e. elements smaller than pivot
                if (i < high) quicksort(a, i, high); //sorts the right segment, i.e. i.e. elements greater than the pivot
            
        }
    }
    
    /**
     * Resizes the hash table when the load factor exceeds the maximum.
     * This implementation does a complete re-hash of all elements, because their hash values - and, thus, their positions, will change with the new table size. 
     */
    private void resizeTable(){
        System.out.println("RESIZING");
        LinkedList<Contact>[] oldTable = table; //store reference to the old table
        int oldTableCapacity = tableCapacity; //and its capacity
        primeIndex++; //inc to select... 
        
        tableCapacity = PRIME_SIZES[primeIndex]; //the next prime size for the new table size.
        System.out.println("resizing to " + tableCapacity);
        
        //Create new table, i.e. array of LL objects, with increased capacity
        table = new LinkedList[tableCapacity];
        for (int i = 0; i < tableCapacity; i++){ //for each element in the new array...
            table[i] = new LinkedList<>(); //initialise with an empty LL
        }
        
        int oldEntries = numEntries; //save the old entry count
        numEntries = 0; //reset the current count to zero.
        
        //Re-hash all entries from old table
        for (int i = 0; i < oldTableCapacity; i++){ //iterate through each bucket in the old table
            LinkedList<Contact> chain = oldTable[i];
            if (chain != null && !chain.isEmpty()){ //for each non-empty bucket, iterate through its chain.
                for (Contact contact : chain) { //Add each contact to the new table... (this for each loop to iterate through all contacts in a chain)
                    putWithoutResizing(contact); //by calling putWithoutResizing. 
                }
            }
        }
        
        assert numEntries == oldEntries : "Entry count changed during resize"; //Verify that the entry count after resizing matches the original count.
    }
}        
        
    
    //##### OLD IMPLEMENTATION CODE KEPT HERE TEMPORARILY FOR REFERENCE IN CASE RETROACTIVE TESTING IS CONDUCTED ON PRIOR STAGES ####
    /**Locates the position in the hash table for a given Contact object denoted
     * by name. 'Lazy deletion' implemented alongside quadratic probing.
     * Maintains neccessary chains for collision resolution with quad probing.
     * Could be augmented in future to minimise clustering by 'remembering',
     * for possible re-use, the deleted positions.
     * 
     * @param name : the name of the Contact object to be located in the HT
     * @return : the position where that name is found, or the first truly empty
     * (null) encountered while probing where the name may be inserted to the HT.
     **/
    /*private int findPos(String name) {
        assert name != null && !name.trim().equals(""); //trim whitespace and verify name is not null or an empty string
        int startPos = hash(name); //Calculate initial hash position 
        int pos = startPos; //init the position being examined to the initial hash position
        int numVisited = 1; //init counter for num positions checked
        //Debugger print statements
        
        System.out.println("\nStarting search for: " + name);
        System.out.println("Initial hash position: " + startPos);
        

        
        //check whether the initial position is either empy (null - maybe change
        //later...), or contains target.
        //If so for either, no probing needed so can skip to return
        if (table[pos] == null){
            System.out.println("Search ended at position " + pos + "(empty)");
            totalVisited += numVisited;
            return pos;
            
        //or, if the target was found at the initial position, and has not been
        //deleted, no need for probing either, so return that position.     
        } else if (table[pos]!= DELETED && name.equals(table[pos].getName())) {
                System.out.println("Search ended at position " + pos + "(found: " + table[pos].getName() + ")");
                totalVisited += numVisited;
                return pos;                
        }

        //commence quad probing if the initial position is not appropriate.
        for (int i=1; numVisited < table.length; i++){
            //keep track of bucket trail for analysis
           System.out.println("Visiting bucket " + pos + ": " + table[pos] );
           
           /**apply the quadratic formula h(k,i) = (h'(k) + i^2) mod m , where:
            * h'(k) is the initit hash value for key, denoted by startPos
            * i denotes the number representing the current probe attempt count
            * i^2 represents the step size, facilitating quadratic expansion 
            * m is the table capacity (set elsewhere to prime), i.e. table.length
            * the result being the next pos in the table to be checked.
            ////////
           int step = i * i;
           //Calculate the new position using quad formula
           //Double up the mod to ensure positive result even if overflow leads
           //the first statement to yield a negative
           pos = ((startPos + step) % table.length + table.length) % table.length;
           numVisited++;//inc counter 
           //Debugger print stmnt
           System.out.println("Next step size: " + step + ",moving to position: " + pos);
           
           if (table[pos] == null){
              break;
           //or, if the target has been found...    
           }else if (table[pos] != DELETED && name.equals(table[pos].getName())){
               break;
              
           }
           
        }
        //output trail and result for analysis  
        //System.out.print("Search ended at position " +pos);
        if (table[pos] != null && table[pos] != DELETED){
            System.out.println("Found " + table[pos].getName());
        } else {
            System.out.println("Position is empty or has been deleted");
        }
        System.out.println("number of  buckets visited = " + numVisited);
        totalVisited += numVisited;
      
        //assert table[pos] == null || name.equals(table[pos].getName());
        return pos;
    }
    
    */

    
     /* Determines whether a Contact name exists as a key inside the database
     *
     * @pre name not null or empty string
     * @param name the Contact name (key) to locate
     * @return true iff the name exists as a key in the database to a non-DELETED
     * slot
     */
    //public boolean containsName(String name) {
    //    assert name != null && !name.equals("");
    //    Contact contact = get(name);
    //    return contact != null && contact != DELETED;
    //}

    /**
     * Returns a Contact object mapped to the supplied name.
     *
     * @pre name not null or empty string
     * @param name The Contact name (key) to locate
     * @return the Contact object mapped to the key name if the name exists as
     * key in the database, otherwise null
     */
    //@Override
    //public Contact get(String name) {
    //    assert name != null && !name.trim().equals("");
    //    int pos = findPos(name);
    //    //null is returned for both empty and DELETED entries
    //    if (table[pos] == null || table[pos] == DELETED) {
    //        return null; // not found
    //    } 
    //    return  table[pos];       
    //}

    /**
     * Returns the number of contacts in the database
     *
     * @pre true
     * @return number of contacts in the database. 0 if empty
     */
    //public int size() {return numEntries; }

    /**
     * Determines if the database is empty or not.
     *
     * @pre true
     * @return true iff the database is empty
     */
    //@Override
    //public boolean isEmpty() {return numEntries == 0; }

    
    /**private Contact putWithoutResizing(Contact contact) {
      String name = contact.getName();
      int pos = findPos(name);
      Contact previous;
      assert table[pos] == null || name.equals(table[pos].getName());
      previous = table[pos]; // old value
      if (previous == null) { // new entry
         table[pos] = contact;
         numEntries++;
      } else {
         table[pos] = contact; // overwriting 
         
      }
      return previous;
   }*/
    
    /**
     * Inserts a contact object into the database, with the key of the supplied
     * contact's name. Note: If the name already exists as a key, then then the
     * original entry is overwritten. This method should return the previous
     * associated value if one exists, otherwise null
     *
     * @pre contact not null or empty string
     */
    /**public Contact put(Contact contact) {
        assert contact != null;
       Contact previous;
        String name = contact.getName();
        assert name != null && !name.trim().equals("");
        previous =  putWithoutResizing(contact);
        if (previous == null && loadFactor() > maxLoadFactor) resizeTable();
        return previous;
    }*/

    /**
     * Removes and returns a contact from the database, with the key the
     * supplied name, using lazy deletion.
     *
     * @param name The name (key) to remove.
     * @pre name not null or empty string
     * @return the removed contact object mapped to the name, or null if the
     * name does not exist.
     */
    /**public Contact remove(String name) {
        assert name != null && !name.trim().equals("");
        int pos = findPos(name);
        Contact removedContact = null; // initialise
        
        //Only 'DELETE' if a non-null, non-DELETED, matching entry
        if (table[pos] != null && table[pos] != DELETED && name.equals(table[pos].getName())){
            removedContact = table[pos];
            table[pos] = DELETED; //rather than using null, mark as DELETED
            numEntries--; //decrement size, replicating the removal of an entry
        }
        
        return removedContact;
    }
    */

    /**
     * Prints the names and IDs of all the contacts in the database in
     * alphabetic order.
     *
     * @pre true
     */
    /**public void displayDB() {
        // not yet ordered
        System.out.println("capacity " + table.length + " size " + numEntries
                + " Load factor " + loadFactor() + "%");
        for (int i = 0; i != table.length; i++) {
            if (table[i] != null) 
                System.out.println(i + " " + table[i].toString());
            else
                 System.out.println(i + " " + "_____");
            }
        
        
        Contact[] toBeSortedTable = new Contact[tableCapacity];  // OK to use Array.sort
        int j = 0;
        for (int i = 0; i != table.length; i++) {
            if (table[i] != null) {
                toBeSortedTable[j] = table[i];
                j++;
            }
        }
        quicksort(toBeSortedTable, 0, j - 1);
        for (int i = 0; i != j; i++) {
            System.out.println(i + " " + " " + toBeSortedTable[i].toString());
        }
    }

    private void quicksort(Contact[] a, int low, int high) {
        assert a != null && 0 <= low && low <= high && high < a.length;
        int i = low, j = high;
        Contact temp;
        if (high >= 0) { // can't get pivot for empty sequence
            String pivot = a[(low + high) / 2].getName();
            while (i <= j) {
                while (a[i].getName().compareTo(pivot) < 0) i++;
                while (a[j].getName().compareTo(pivot) > 0) j--;
                // forall k :low ..i -1: a[k] < pivot && 
                // forall k: j+1 .. high: a[k] > pivot &&
                // a[i] >= pivot && a[j] <= pivot
                if (i <= j) {
                    temp = a[i]; a[i] = a[j]; a[j] = temp;
                    i++; j--;
                }
                if (low < j) quicksort(a, low, j); // recursive call 
                if (i < high) quicksort(a, i, high); // recursive call 
            }
        }
    }
    /**
     * When loadFactor > the assigned max, resizes the hash table, carrying over 
     * only non-DELETED entries, clearing deletion markers and rehashing all the
     * new entries into the new, expanded table. 
     */
    //private void resizeTable() { // mkae a new table of greater capacity and rehashes old values into it
    //    System.out.println("RESIZING");
    //    Contact[] oldTable = table; // copy the reference
    //    int oldTableCapacity = tableCapacity;
    //    primeIndex++;
        
    //    tableCapacity = PRIME_SIZES[primeIndex];
    //    System.out.println("resizing to " + tableCapacity);
    //    table = new Contact[tableCapacity]; // make a new table
        
    //    clearDB();
    //    numEntries = 0;
    //    for (int i = 0; i != oldTableCapacity; i++) {
    //        if (oldTable[i] != null && oldTable[i] != DELETED) { // dleted vakues not hashed across
    //            putWithoutResizing(oldTable[i]);
    //        }
    //    }
        
    //}
//} 
//##################################################################
