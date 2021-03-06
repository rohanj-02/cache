
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.ArrayList;

public class cache{

    static int WORD_LENGTH = 32;

    //Validation functions

    static boolean isBinary(String s){
        for(int i = 0; i < s.length(); i++){
            char c = s.charAt(i);
            if( !(c == '0' || c == '1')){
                return false;
            }
        }
        return true;
    }

    static boolean isHex(String s){
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
	    if ((Math.abs(c - '0') < 10 ) || (Math.abs(c - 'A') < 6) || (Math.abs(c - 'a') < 6)) 
		return true;
	    
        }
        return false;
    }

    static boolean isPowerOfTwo(int s){
        if(s == 0){
            return false;
        }
        double v = Math.log(s) / Math.log(2);
        return (int)(Math.ceil(v)) == (int)(Math.floor(v));
    }

    //Conversion Functions

    static String hexToBinary(String hex) {
        int i = Integer.parseInt(hex, 16);
        String bin = Integer.toBinaryString(i);
        if(bin.length() != 32){
            String t = "";
            for(i = 0; i < 32 - bin.length(); i++){
                t += "0";
            }
            bin = t + bin;
        }
        return bin;
    }

    static String makeNBit(String x, int n){
        if(x.length() != n){
            String t = "";
            for(int i = 0; i < n - x.length(); i++){
                t += "0";
            }
            x = t + x;
        }
        return x;
    }

    static int log2(int N){
        int result = (int)(Math.log(N) / Math.log(2));
        return result;
    }

    static int binaryToDecimal(String x){
        return Integer.parseInt(x, 2);
    }

    //Custom Classes

    static class Memory{
        String address; //full address in binary
        String data;

        Memory(){
            this.address = "";
            this.data = "NULLNULL";
        }

        Memory(String address){
            this.address = address;
            this.data = "NULLNULL";
        }

        Memory(String address, String data){
            this.address = address;
            this.data = data;
        }

        void printMemory(){
            System.out.print(makeNBit(Integer.toHexString(Integer.parseInt(address, 2)), 8) + " " + data + " ");
        }
    }

    static class Block{
        Memory[] data;
        int size;

        Block(int size){
            this.size = size;
            this.data = new Memory[size];
            for(int i = 0; i < size; i++){
                this.data[i] = new Memory();
            }
        }

        Block(int size, int address, String value, String blockAddress, int offsetBits){
            this.size = size;
            this.data = new Memory[size];
            for(int i = 0; i < size; i++){
                if(i == address){
                    this.data[i] = new Memory(blockAddress + makeNBit(Integer.toString(address, 2), offsetBits), value);
                }
                else{
                    this.data[i] = new Memory(blockAddress + makeNBit(Integer.toString(i, 2), offsetBits));
                }
            }
        }

        void printBlock(int elementsInOneLine){
            if (log2(elementsInOneLine) % 2 == 0) {
                elementsInOneLine = (int)Math.sqrt(elementsInOneLine);
            }
            else{
                elementsInOneLine = (int)Math.sqrt(elementsInOneLine * 2);
            }
            for(int i = 1; i <= size; i++){
                data[i - 1].printMemory();
                if(i % elementsInOneLine == 0){
                    System.out.println();
                }
            }
            System.out.println();
        }
    }

    static class BlockMem{
        String tagPart;
        Block block;

        BlockMem(String tagPart, Block block){
            this.tagPart = tagPart;
            this.block = block;
        }

    }

    //Direct Mapping

    static class directMapped{
        //Global Variables
        int OFFSET_BITS;
        int TAG_BITS;
        int INDEX_BITS;
        int BLOCK_SIZE;
        ArrayList<String> tagArray;
        ArrayList<Block> dataArray;
        ArrayList<Boolean> isValidArray;
        ArrayList<BlockMem> mainMemory = new ArrayList<BlockMem>();

        //Functions

        void loadIntoCache(int indexDecimal, String tagPart, int offsetDecimal, String value){
            //loads value to cache given set has space
            System.out.println("Loading into Cache...");
            tagArray.set(indexDecimal, tagPart);
            isValidArray.set(indexDecimal, true);
            dataArray.set(indexDecimal, new Block(BLOCK_SIZE, offsetDecimal, value, tagPart + makeNBit(Integer.toString(indexDecimal, 2), INDEX_BITS), OFFSET_BITS));

        }

        void replaceIntoCache(int indexDecimal, String tagPart, int offsetDecimal, String value){

            Block toBeAdded = new Block(BLOCK_SIZE, offsetDecimal, value, tagPart + makeNBit(Integer.toString(indexDecimal, 2), INDEX_BITS), OFFSET_BITS);
            for(int i = 0; i < mainMemory.size(); i++){
                if(mainMemory.get(i).tagPart.equals(tagPart + makeNBit(Integer.toString(indexDecimal, 2), INDEX_BITS))){
                    toBeAdded = mainMemory.get(i).block;
                    mainMemory.remove(i);
                }
            }

            Block blockTemp = dataArray.get(indexDecimal);
            String tagTemp = tagArray.get(indexDecimal) + makeNBit(Integer.toString(indexDecimal, 2), INDEX_BITS);
            BlockMem temp = new BlockMem(tagTemp, blockTemp);
            mainMemory.add(temp);
            String dataToBeReplaced = tagTemp;
            for(int j = 0; j < 32 - tagTemp.length(); j++){
                dataToBeReplaced += "0";
            }
            System.out.println("Block Memory to be replaced: " + Integer.toString(Integer.parseInt(dataToBeReplaced, 2), 16));
            tagArray.set(indexDecimal, tagPart);
            dataArray.set(indexDecimal, toBeAdded);
        }

        void readCache(int offsetDecimal, int indexDecimal){
            System.out.println("Read hit");
            Block fetchedBlock = dataArray.get(indexDecimal);
            System.out.println("Data : " + fetchedBlock.data[offsetDecimal].data);
        }

        void writeCache(int offsetDecimal, int indexDecimal, String value){
            System.out.println("Write hit");
            dataArray.get(indexDecimal).data[offsetDecimal].data = value;
        }

        void lookup(String address, boolean isRead, String value){
            address = hexToBinary(address);
            value = makeNBit(value, 8);
            String tagPart = address.substring(0,TAG_BITS - INDEX_BITS);
            String indexPart = address.substring(TAG_BITS - INDEX_BITS, TAG_BITS);
            String offsetPart = address.substring(TAG_BITS, address.length());
//        System.out.println("Tag : " + tagPart);
//        System.out.println("Index : " + indexPart);
//        System.out.println("Offset : " + offsetPart);
            int indexDecimal = binaryToDecimal(indexPart);
            int offsetDecimal = binaryToDecimal(offsetPart);
            boolean isHit = false;

            //Iterate over the concerned set and then if equal print the value

            String concernedSet = tagArray.get(indexDecimal);
            if(concernedSet.equals(tagPart)){
                isHit = true;
                if(isRead){
                    readCache(offsetDecimal, indexDecimal);
                }
                else{
                    System.out.println("Previous Data : " + dataArray.get(indexDecimal).data[offsetDecimal].data);
                    writeCache(offsetDecimal, indexDecimal, value);
                    System.out.println("New data : " + value);
                }
            }
            if(!isHit && !isValidArray.get(indexDecimal)){
                if(isRead){
                    System.out.println("Read Miss");
                    loadIntoCache(indexDecimal, tagPart, offsetDecimal, value); //value = null for read
                    Block fetchedBlock = dataArray.get(indexDecimal);
                    System.out.println("Data : " + fetchedBlock.data[offsetDecimal].data);
                }
                else{
                    System.out.println("Write Miss");
                    loadIntoCache(indexDecimal, tagPart, offsetDecimal, value);
                    dataArray.get(indexDecimal).data[offsetDecimal].data = value;
                    System.out.println("Data written at " + Integer.toString(Integer.parseInt(address,2),16)); //Converting binary address to hexadecimal address
                }
            }

            else if(!isHit && isValidArray.get(indexDecimal)){
                if(isRead){
                    System.out.println("Read Miss");
                    System.out.println("Replacing...");
                    replaceIntoCache(indexDecimal, tagPart, offsetDecimal, value);
                    Block fetchedBlock = dataArray.get(indexDecimal);
                    System.out.println("Data : " + fetchedBlock.data[offsetDecimal].data);
                }
                else{
                    System.out.println("Write Miss");
                    System.out.println("Replacing...");
                    replaceIntoCache(indexDecimal, tagPart, offsetDecimal, value);
                    dataArray.get(indexDecimal).data[offsetDecimal].data = value;
                    System.out.println("Data written at " + Integer.toString(Integer.parseInt(address,2),16)); //Converting binary address to hexadecimal address
                }
            }
        }

        void printCache(){
            for(int i = 0; i < tagArray.size(); i++) {
                if(!isValidArray.get(i)) {
                    System.out.println("Index Number : " + i);
                    System.out.println("Empty");
                }
                else{
                    System.out.println("Index Number : " + i);
                    String t = tagArray.get(i) + makeNBit(Integer.toString(i, 2), INDEX_BITS);
                    for (int k = 0; k < OFFSET_BITS; k++) {
                        t += "0";
                    }
                    System.out.println("Block Address : " + Integer.toString(Integer.parseInt(t, 2), 16));
                    dataArray.get(i).printBlock(BLOCK_SIZE);
                }
            }
        }

        void printMainMemory(){
            for(int i = 0; i < mainMemory.size(); i++){
                System.out.println("Tag : " + mainMemory.get(i).tagPart);
                mainMemory.get(i).block.printBlock(BLOCK_SIZE);
            }
            System.out.println("SIze main mem : " + mainMemory.size());
        }

        void directRunner(Scanner s){
            //INPUT
            int cacheSizeInKB = 0;
            int cacheLine = 0;
            BLOCK_SIZE = 0;
            boolean flag = false;
            //INPUT SIZE OF CACHE
            do{
                try{
                    System.out.println("Enter size of cache in KB : ");
                    cacheSizeInKB = s.nextInt();
                    flag = !isPowerOfTwo(cacheSizeInKB);
                    if(flag){
                        System.out.println("Error : Cache Size should be a power of two.");
                    }
                    else{
                        flag = false;
                    }
                }
                catch(InputMismatchException e) {
                    System.out.println("Error : " + e);
                    s.nextLine();
                    flag = true;
                }
            }while(flag);
            //INPUT CACHE LINES
            do{
                try{
                    System.out.println("Enter number of cache lines : ");
                    cacheLine = s.nextInt();
                    flag = !isPowerOfTwo(cacheLine);
                    if(flag){
                        System.out.println("Error : Cache Lines should be a power of two.");
                    }
                    else{
                        flag = false;
                    }
                }
                catch(InputMismatchException e) {
                    System.out.println("Error : " + e);
                    s.nextLine();
                    flag = true;
                }
            }while(flag);
            //INPUT SIZE OF BLOCK
            do{
                try{
                    System.out.println("Enter size of block in Bytes : ");
                    BLOCK_SIZE = s.nextInt();
                    flag = !isPowerOfTwo(BLOCK_SIZE);

                    if(flag){
                        System.out.println("Error : Block Size should be a power of two.");
                    }
                    else if(cacheSizeInKB * Math.pow(2, 10)/cacheLine != BLOCK_SIZE){
                        System.out.println("Error : Block Size should be cache size divided by cache line");
                        flag = true;
                    }
                    else if(cacheSizeInKB * Math.pow(2,10) < BLOCK_SIZE){
                        System.out.println("Error : Block Size cannot be greater than cache size");
                        flag = true;
                    }
                    else{
                        flag = false;
                    }
                }
                catch(InputMismatchException e) {
                    System.out.println("Error : " + e);
                    s.nextLine();
                    flag = true;
                }
            }while(flag);

            //FOR CUSTOM TEST CASE
//            int cacheSizeInKB = 8;
//            BLOCK_SIZE = 64;
//            NO_OF_ELEMENTS_IN_SET = 4;

            OFFSET_BITS = (int)log2(BLOCK_SIZE);
            TAG_BITS = WORD_LENGTH - OFFSET_BITS;
            int sizeOfTag = (int)Math.pow(2, log2(cacheSizeInKB) + 10 - log2(BLOCK_SIZE));
            INDEX_BITS = (int)log2(sizeOfTag);
            tagArray = new ArrayList<String>();
            dataArray = new ArrayList<Block>();
            isValidArray = new ArrayList<Boolean>();

            for(int i = 0; i < sizeOfTag; i++){
                tagArray.add("");
                dataArray.add(new Block(BLOCK_SIZE));
                isValidArray.add(false);
            }

            //FOR CUSTOM TEST CASE
//            lookup("1000", false, "0101");
//            lookup("2000", false, "0001");
//            lookup("3000", false, "1001");
//            lookup("4000", false, "1101");

            flag = false;
            //ACTUAL RUNNER
            do{
                try{
                    System.out.println("Enter the function that you want to perform(read, write, print, exit) : ");
                    String mode = s.next();
                    mode = mode.trim();
                    mode = mode.toLowerCase();
                    flag = true;
                    //READ MODE
                    if(mode.equals("read")){
                        boolean insideFlag = false;
                        do{
                            try{
                                System.out.println("Enter address in hexadecimal : ");
                                String h = s.next();
                                if(h.substring(0,2).equals("0x") || h.substring(0,2).equals("0X")){
                                    h = h.substring(2);
                                }
                                if(h.length() > 8){
                                    System.out.println("Error : The address should be 32 bit. ");
                                    insideFlag = true;
                                }
                                else if(!isHex(h)){
                                    System.out.println("Error : Address is not in hexadecimal format!");
                                    insideFlag = true;
                                }
                                else{
                                    lookup(h, true, "NULLNULL");
                                    insideFlag = false;
                                }
                            }
                            catch(InputMismatchException e){
                                System.out.println("Error : " + e);
                                insideFlag = true;
                                s.nextLine();
                            }
                        }while(insideFlag);
                    }
                    //WRITE MODE
                    else if(mode.equals("write")){
                        boolean insideFlag = false;
                        do{
                            try{
                                System.out.println("Enter address in hexadecimal and data in Binary: ");
                                String h = s.next();
                                String data = s.next();
                                //HEXADECIMAL VALIDATION
                                if(h.substring(0,2).equals("0x") || h.substring(0,2).equals("0X")){
                                    h = h.substring(2);
                                }
                                if(h.length() > 8){
                                    System.out.println("Error : The address should be 32 bit. ");
                                    insideFlag = true;
                                }
                                else if(!isHex(h)){
                                    System.out.println("Error : Address should be in hexadecimal format!");
                                    insideFlag = true;
                                }
                                //DATA VALIDATION
                                else if(data.length() > 8){
                                    System.out.println("Error : Data cannot be more than 8 bits.");
                                    insideFlag = true;
                                }
                                else if(!isBinary(data)){
                                    System.out.println("Error : Data should be in binary format.");
                                    insideFlag = true;
                                }
                                else{
                                    lookup(h, false, data);
                                    insideFlag = false;
                                }
                            }
                            catch(InputMismatchException e){
                                System.out.println("Error : " + e);
                                insideFlag = true;
                                s.nextLine();
                            }
                        }while(insideFlag);
                    }
                    //PRINT MODE
                    else if(mode.equals("print")){
                        printCache();
                    }
                    //EXIT
                    else if(mode.equals("exit")){
                        flag = false;
                    }
                    //MAIN MEMORY
                    else if(mode.equals("main")){
                        printMainMemory();
                    }
                    //INVALID
                    else{
                        System.out.println("Error : Invalid Input.");
                        flag = true;
                    }
                }
                catch(InputMismatchException e){
                    System.out.println("Error : " + e);
                    s.nextLine();
                    flag = true;
                }
            }while(flag);
        }
    }

    //Fully Associative Cache

    static class fullyAssociative{
        //Global Variables
        int OFFSET_BITS;
        int sizeOfTag;
        int TAG_BITS;
        int BLOCK_SIZE;
        int ELEMENTS_IN_TAG = 0;
        ArrayList<String> tagArray;
        ArrayList<Block> dataArray;
        ArrayList<Boolean> isValidArray;
        ArrayList<BlockMem> mainMemory = new ArrayList<BlockMem>();

        //Functions

        void loadIntoCache(String tagPart, int offsetDecimal, String value){
            //loads value to cache given set has space
            System.out.println("Loading into Cache...");
//            isValidArray.remove(tagArray.size() - 1);
//            tagArray.remove(tagArray.size() - 1);
//            dataArray.remove(tagArray.size() - 1);
            tagArray.add(0, tagPart);
            dataArray.add(0, new Block(BLOCK_SIZE, offsetDecimal, value, tagPart , OFFSET_BITS));
            isValidArray.add(0, true);
            ELEMENTS_IN_TAG ++;
        }

        void replaceIntoCache(String tagPart, int offsetDecimal, String value){

            Block toBeAdded = new Block(BLOCK_SIZE, offsetDecimal, value, tagPart, OFFSET_BITS);
            for(int i = 0; i < mainMemory.size(); i++){
                if(mainMemory.get(i).tagPart.equals(tagPart)){
                    toBeAdded = mainMemory.get(i).block;
                    mainMemory.remove(i);
                }
            }

            Block blockTemp = dataArray.get(tagArray.size() - 1);
            String tagTemp = tagArray.get(tagArray.size() - 1);
            BlockMem temp = new BlockMem(tagTemp, blockTemp);
            mainMemory.add(temp);
            String dataToBeReplaced = tagTemp;
            for(int j = 0; j < 32 - tagTemp.length(); j++){
                dataToBeReplaced += "0";
            }
            System.out.println("Block Memory to be replaced: " + Integer.toString(Integer.parseInt(dataToBeReplaced, 2), 16));
            tagArray.remove(tagArray.size() - 1);
            dataArray.remove(dataArray.size() - 1);
            isValidArray.remove(isValidArray.size() - 1);
            isValidArray.add(0, true);
            tagArray.add(0, tagPart);
            dataArray.add(0, toBeAdded);
        }

        void readCache(int offsetDecimal, int indexDecimal){
            System.out.println("Read hit");
            Block fetchedBlock = dataArray.get(indexDecimal);
            System.out.println("Data : " + fetchedBlock.data[offsetDecimal].data);
        }

        void writeCache(int offsetDecimal, int indexDecimal, String value){
            System.out.println("Write hit");
            dataArray.get(indexDecimal).data[offsetDecimal].data = value;
        }

        void lookup(String address, boolean isRead, String value){
            address = hexToBinary(address);
            value = makeNBit(value, 8);
            String tagPart = address.substring(0,TAG_BITS);
            String offsetPart = address.substring(TAG_BITS, address.length());
            int indexDecimal = 0;
            int offsetDecimal = binaryToDecimal(offsetPart);
            boolean isHit = false;

            //Iterate over the concerned set and then if equal print the value

            for(int i = 0; i < tagArray.size(); i++){
                if(tagArray.get(i).equals(tagPart)){
                    isHit = true;
                    indexDecimal = i;
                    if(isRead){
                        readCache(offsetDecimal, indexDecimal);
                    }
                    else{
                        System.out.println("Previous Data : " + dataArray.get(indexDecimal).data[offsetDecimal].data);
                        writeCache(offsetDecimal, indexDecimal, value);
                        System.out.println("New data : " + value);
                    }
                    Block fetchedBlock = dataArray.get(indexDecimal);
                    //for lru implementation
                    tagArray.remove(i);
                    tagArray.add(0, tagPart);
                    dataArray.remove(i);
                    dataArray.add(0, fetchedBlock);
                }
            }

            if(!isHit && ELEMENTS_IN_TAG < sizeOfTag){
                if(isRead){
                    System.out.println("Read Miss");
                    loadIntoCache(tagPart, offsetDecimal, value); //value = null for read
                    Block fetchedBlock = dataArray.get(0);
                    System.out.println("Data : " + fetchedBlock.data[offsetDecimal].data);
                }
                else{
                    System.out.println("Write Miss");
                    loadIntoCache(tagPart, offsetDecimal, value);
                    dataArray.get(0).data[offsetDecimal].data = value;
                    System.out.println("Data written at " + Integer.toString(Integer.parseInt(address,2),16)); //Converting binary address to hexadecimal address
                }
            }

            else if(!isHit && ELEMENTS_IN_TAG >= sizeOfTag){
                if(isRead){
                    System.out.println("Read Miss");
                    System.out.println("Replacing...");
                    replaceIntoCache(tagPart, offsetDecimal, value);
                    Block fetchedBlock = dataArray.get(0);
                    System.out.println("Data : " + fetchedBlock.data[offsetDecimal].data);
                }
                else{
                    System.out.println("Write Miss");
                    System.out.println("Replacing...");
                    replaceIntoCache(tagPart, offsetDecimal, value);
                    dataArray.get(0).data[offsetDecimal].data = value;
                    System.out.println("Data written at " + Integer.toString(Integer.parseInt(address,2),16)); //Converting binary address to hexadecimal address
                }
            }
        }

        void printCache(){
            for(int i = 0; i < tagArray.size(); i++) {
                if(!isValidArray.get(i)) {
                    System.out.println("Index Number : " + i);
                    System.out.println("Empty");
                }
                else{
                    System.out.println("Index Number : " + i);
                    String t = tagArray.get(i);
                    for (int k = 0; k < OFFSET_BITS; k++) {
                        t += "0";
                    }
                    System.out.println("Block Address : " + Integer.toString(Integer.parseInt(t, 2), 16));
                    dataArray.get(i).printBlock(BLOCK_SIZE);
                }
            }
        }

        void printMainMemory(){
            for(int i = 0; i < mainMemory.size(); i++){
                System.out.println("Tag : " + mainMemory.get(i).tagPart);
                mainMemory.get(i).block.printBlock(BLOCK_SIZE);
            }
        }

        void fullyRunner(Scanner s){
            //INPUT
            int cacheSizeInKB = 0;
            int cacheLine = 0;
            BLOCK_SIZE = 0;
            boolean flag = false;
            //INPUT SIZE OF CACHE
            do{
                try{
                    System.out.println("Enter size of cache in KB : ");
                    cacheSizeInKB = s.nextInt();
                    flag = !isPowerOfTwo(cacheSizeInKB);
                    if(flag){
                        System.out.println("Error : Cache Size should be a power of two.");
                    }
                    else{
                        flag = false;
                    }
                }
                catch(InputMismatchException e) {
                    System.out.println("Error : " + e);
                    s.nextLine();
                    flag = true;
                }
            }while(flag);
            //INPUT CACHE LINES
            do{
                try{
                    System.out.println("Enter number of cache lines : ");
                    cacheLine = s.nextInt();
                    flag = !isPowerOfTwo(cacheLine);
                    if(flag){
                        System.out.println("Error : Cache Lines should be a power of two.");
                    }
                    else{
                        flag = false;
                    }
                }
                catch(InputMismatchException e) {
                    System.out.println("Error : " + e);
                    s.nextLine();
                    flag = true;
                }
            }while(flag);
            //INPUT SIZE OF BLOCK
            do{
                try{
                    System.out.println("Enter size of block in Bytes : ");
                    BLOCK_SIZE = s.nextInt();
                    flag = !isPowerOfTwo(BLOCK_SIZE);

                    if(flag){
                        System.out.println("Error : Block Size should be a power of two.");
                    }
                    else if(cacheSizeInKB * Math.pow(2, 10)/cacheLine != BLOCK_SIZE){
                        System.out.println("Error : Block Size should be cache size divided by cache line");
                        flag = true;
                    }
                    else if(cacheSizeInKB * Math.pow(2,10) < BLOCK_SIZE){
                        System.out.println("Error : Block Size cannot be greater than cache size");
                        flag = true;
                    }
                    else{
                        flag = false;
                    }
                }
                catch(InputMismatchException e) {
                    System.out.println("Error : " + e);
                    s.nextLine();
                    flag = true;
                }
            }while(flag);

            //FOR CUSTOM TEST CASE
//            int cacheSizeInKB = 8;
//            BLOCK_SIZE = 64;
//            NO_OF_ELEMENTS_IN_SET = 4;

            OFFSET_BITS = (int)log2(BLOCK_SIZE);
            TAG_BITS = WORD_LENGTH - OFFSET_BITS;
            sizeOfTag = cacheLine;
            tagArray = new ArrayList<String>();
            dataArray = new ArrayList<Block>();
            isValidArray = new ArrayList<Boolean>();
	    
            //FOR CUSTOM TEST CASE
//            lookup("1000", false, "0101");
//            lookup("2000", false, "0001");
//            lookup("3000", false, "1001");
//            lookup("4000", false, "1101");

            flag = false;
            //ACTUAL RUNNER
            do{
                try{
                    System.out.println("Enter the function that you want to perform(read, write, print, exit) : ");
                    String mode = s.next();
                    mode = mode.trim();
                    mode = mode.toLowerCase();
                    flag = true;
                    //READ MODE
                    if(mode.equals("read")){
                        boolean insideFlag = false;
                        do{
                            try{
                                System.out.println("Enter address in hexadecimal : ");
                                String h = s.next();
                                if(h.substring(0,2).equals("0x") || h.substring(0,2).equals("0X")){
                                    h = h.substring(2);
                                }
                                if(h.length() > 8){
                                    System.out.println("Error : The address should be 32 bit. ");
                                    insideFlag = true;
                                }
                                else if(!isHex(h)){
                                    System.out.println("Error : Address is not in hexadecimal format!");
                                    insideFlag = true;
                                }
                                else{
                                    lookup(h, true, "NULLNULL");
                                    insideFlag = false;
                                }
                            }
                            catch(InputMismatchException e){
                                System.out.println("Error : " + e);
                                insideFlag = true;
                                s.nextLine();
                            }
                        }while(insideFlag);
                    }
                    //WRITE MODE
                    else if(mode.equals("write")){
                        boolean insideFlag = false;
                        do{
                            try{
                                System.out.println("Enter address in hexadecimal and data in Binary: ");
                                String h = s.next();
                                String data = s.next();
                                //HEXADECIMAL VALIDATION
                                if(h.substring(0,2).equals("0x") || h.substring(0,2).equals("0X")){
                                    h = h.substring(2);
                                }
                                if(h.length() > 8){
                                    System.out.println("Error : The address should be 32 bit. ");
                                    insideFlag = true;
                                }
                                else if(!isHex(h)){
                                    System.out.println("Error : Address should be in hexadecimal format!");
                                    insideFlag = true;
                                }
                                //DATA VALIDATION
                                else if(data.length() > 8){
                                    System.out.println("Error : Data cannot be more than 8 bits.");
                                    insideFlag = true;
                                }
                                else if(!isBinary(data)){
                                    System.out.println("Error : Data should be in binary format.");
                                    insideFlag = true;
                                }
                                else{
                                    lookup(h, false, data);
                                    insideFlag = false;
                                }
                            }
                            catch(InputMismatchException e){
                                System.out.println("Error : " + e);
                                insideFlag = true;
                                s.nextLine();
                            }
                        }while(insideFlag);
                    }
                    //PRINT MODE
                    else if(mode.equals("print")){
                        printCache();
                    }
                    //EXIT
                    else if(mode.equals("exit")){
                        flag = false;
                    }
                    //MAIN MEMORY
                    else if(mode.equals("main")){
                        printMainMemory();
                    }
                    //INVALID
                    else{
                        System.out.println("Error : Invalid Input.");
                        flag = true;
                    }
                }
                catch(InputMismatchException e){
                    System.out.println("Error : " + e);
                    s.nextLine();
                    flag = true;
                }
            }while(flag);
        }
    }

    //N-Way Set Associative Cache

    static class nWayAssociative{
        //Global Variables
        int OFFSET_BITS;
        int TAG_BITS;
        int INDEX_BITS;
        int NO_OF_ELEMENTS_IN_SET;
        int BLOCK_SIZE;
        ArrayList<ArrayList<String>> tagArray;
        ArrayList<ArrayList<Block>> dataArray;
        ArrayList<BlockMem> mainMemory = new ArrayList<BlockMem>();

        //Functions

        void loadIntoCache(int indexDecimal, String tagPart, int offsetDecimal, String value){
            //loads value to cache given set has space
            System.out.println("Loading into Cache...");
            tagArray.get(indexDecimal).add(0, tagPart);
            dataArray.get(indexDecimal).add(0, new Block(BLOCK_SIZE, offsetDecimal, value, tagPart + makeNBit(Integer.toString(indexDecimal, 2), INDEX_BITS), OFFSET_BITS));

        }

        void replaceIntoCache(int indexDecimal, String tagPart, int offsetDecimal, String value){

            Block toBeAdded = new Block(BLOCK_SIZE, offsetDecimal, value, tagPart + makeNBit(Integer.toString(indexDecimal, 2), INDEX_BITS), OFFSET_BITS);
            for(int i = 0; i < mainMemory.size(); i++){
                if(mainMemory.get(i).tagPart.equals(tagPart + makeNBit(Integer.toString(indexDecimal, 2), INDEX_BITS))){
                    toBeAdded = mainMemory.get(i).block;
                    mainMemory.remove(i);
                }
            }

            Block blockTemp = dataArray.get(indexDecimal).get(NO_OF_ELEMENTS_IN_SET - 1);
            String tagTemp = tagArray.get(indexDecimal).get(NO_OF_ELEMENTS_IN_SET - 1) + makeNBit(Integer.toString(indexDecimal, 2), INDEX_BITS);
            BlockMem temp = new BlockMem(tagTemp, blockTemp);
            mainMemory.add(temp);
            String dataToBeReplaced = tagTemp;
            for(int j = 0; j < 32 - tagTemp.length(); j++){
                dataToBeReplaced += "0";
            }
            System.out.println("Block Memory to be replaced: " + Integer.toString(Integer.parseInt(dataToBeReplaced, 2), 16));
            tagArray.get(indexDecimal).remove(NO_OF_ELEMENTS_IN_SET - 1);
            dataArray.get(indexDecimal).remove(NO_OF_ELEMENTS_IN_SET - 1);
            tagArray.get(indexDecimal).add(0, tagPart);
            dataArray.get(indexDecimal).add(0, toBeAdded);
        }

        void readCache(int offsetDecimal, int indexDecimal, int index){
            System.out.println("Read hit");
            Block fetchedBlock = dataArray.get(indexDecimal).get(index);
            System.out.println("Data : " + fetchedBlock.data[offsetDecimal].data);
        }

        void writeCache(int offsetDecimal, int indexDecimal, int index, String value){
            System.out.println("Write hit");
            dataArray.get(indexDecimal).get(index).data[offsetDecimal].data = value;
        }

        void lookup(String address, boolean isRead, String value){
            address = hexToBinary(address);
            value = makeNBit(value, 8);
            String tagPart = address.substring(0,TAG_BITS - INDEX_BITS);
            String indexPart = address.substring(TAG_BITS - INDEX_BITS, TAG_BITS);
            String offsetPart = address.substring(TAG_BITS, address.length());
//        System.out.println("Tag : " + tagPart);
//        System.out.println("Index : " + indexPart);
//        System.out.println("Offset : " + offsetPart);
            int indexDecimal = binaryToDecimal(indexPart);
            int offsetDecimal = binaryToDecimal(offsetPart);
            boolean isHit = false;

            //Iterate over the concerned set and then if equal print the value

            ArrayList<String> concernedSet = tagArray.get(indexDecimal);
            for(int i = 0; i < concernedSet.size(); i++){
                if(concernedSet.get(i).equals(tagPart)){
                    isHit = true;
                    if(isRead){
                        readCache(offsetDecimal, indexDecimal, i);
                    }
                    else{
                        System.out.println("Previous Data : " + dataArray.get(indexDecimal).get(i).data[offsetDecimal].data);
                        writeCache(offsetDecimal, indexDecimal, i, value);
                        System.out.println("New data : " + value);
                    }
                    Block fetchedBlock = dataArray.get(indexDecimal).get(i);
                    //for lru implementation
                    tagArray.get(indexDecimal).remove(i);
                    tagArray.get(indexDecimal).add(0, tagPart);
                    dataArray.get(indexDecimal).remove(i);
                    dataArray.get(indexDecimal).add(0, fetchedBlock);
                }
            }

            if(!isHit && concernedSet.size() < NO_OF_ELEMENTS_IN_SET){
                if(isRead){
                    System.out.println("Read Miss");
                    loadIntoCache(indexDecimal, tagPart, offsetDecimal, value); //value = null for read
                    Block fetchedBlock = dataArray.get(indexDecimal).get(0);
                    System.out.println("Data : " + fetchedBlock.data[offsetDecimal].data);
                }
                else{
                    System.out.println("Write Miss");
                    loadIntoCache(indexDecimal, tagPart, offsetDecimal, value);
                    dataArray.get(indexDecimal).get(0).data[offsetDecimal].data = value;
                    System.out.println("Data written at " + Integer.toString(Integer.parseInt(address,2),16)); //Converting binary address to hexadecimal address
                }
            }

            else if(!isHit && concernedSet.size() >= NO_OF_ELEMENTS_IN_SET){
                if(isRead){
                    System.out.println("Read Miss");
                    System.out.println("Replacing...");
                    replaceIntoCache(indexDecimal, tagPart, offsetDecimal, value);
                    Block fetchedBlock = dataArray.get(indexDecimal).get(0);
                    System.out.println("Data : " + fetchedBlock.data[offsetDecimal].data);
                }
                else{
                    System.out.println("Write Miss");
                    System.out.println("Replacing...");
                    replaceIntoCache(indexDecimal, tagPart, offsetDecimal, value);
                    dataArray.get(indexDecimal).get(0).data[offsetDecimal].data = value;
                    System.out.println("Data written at " + Integer.toString(Integer.parseInt(address,2),16)); //Converting binary address to hexadecimal address
                }
            }
        }

        void printCache(){
            for(int i = 0; i < tagArray.size(); i++){
                System.out.println("Set Number : " + i);
                for(int j = 0; j < tagArray.get(i).size(); j++){
                    String t = tagArray.get(i).get(j) + makeNBit(Integer.toString(i, 2), INDEX_BITS);
                    for(int k = 0; k < OFFSET_BITS; k++){
                        t += "0";
                    }
                    System.out.println("Block Address : " + Integer.toString(Integer.parseInt(t, 2), 16));
                    dataArray.get(i).get(j).printBlock(BLOCK_SIZE);
                }
                if(tagArray.get(i).size() == 0){
                    System.out.println("Empty Set");
                }
            }
        }

        void printMainMemory(){
            for(int i = 0; i < mainMemory.size(); i++){
                System.out.println("Tag : " + mainMemory.get(i).tagPart);
                mainMemory.get(i).block.printBlock(BLOCK_SIZE);
            }
        }

        void nWayRunner(Scanner s){
            //INPUT
            int cacheSizeInKB = 0;
            int cacheLine = 0;
            BLOCK_SIZE = 0;
            boolean flag = false;
            //INPUT SIZE OF CACHE
            do{
                try{
                    System.out.println("Enter size of cache in KB : ");
                    cacheSizeInKB = s.nextInt();
                    flag = !isPowerOfTwo(cacheSizeInKB);
                    if(flag){
                        System.out.println("Error : Cache Size should be a power of two.");
                    }
                    else{
                        flag = false;
                    }
                }
                catch(InputMismatchException e) {
                    System.out.println("Error : " + e);
                    s.nextLine();
                    flag = true;
                }
            }while(flag);
            //INPUT CACHE LINES
            do{
                try{
                    System.out.println("Enter number of cache lines : ");
                    cacheLine = s.nextInt();
                    flag = !isPowerOfTwo(cacheLine);
                    if(flag){
                        System.out.println("Error : Cache Lines should be a power of two.");
                    }
                    else{
                        flag = false;
                    }
                }
                catch(InputMismatchException e) {
                    System.out.println("Error : " + e);
                    s.nextLine();
                    flag = true;
                }
            }while(flag);
            //INPUT SIZE OF BLOCK
            do{
                try{
                    System.out.println("Enter size of block in Bytes : ");
                    BLOCK_SIZE = s.nextInt();
                    flag = !isPowerOfTwo(BLOCK_SIZE);

                    if(flag){
                        System.out.println("Error : Block Size should be a power of two.");
                    }
                    else if(cacheSizeInKB * Math.pow(2, 10)/cacheLine != BLOCK_SIZE){
                        System.out.println("Error : Block Size should be cache size divided by cache line");
                        flag = true;
                    }
                    else if(cacheSizeInKB * Math.pow(2,10) < BLOCK_SIZE){
                        System.out.println("Error : Block Size cannot be greater than cache size");
                        flag = true;
                    }
                    else{
                        flag = false;
                    }
                }
                catch(InputMismatchException e) {
                    System.out.println("Error : " + e);
                    s.nextLine();
                    flag = true;
                }
            }while(flag);
            //INPUT N_WAY
            do{
                try{
                    System.out.println("Enter n for n-way set associative cache : ");
                    NO_OF_ELEMENTS_IN_SET = s.nextInt();
                    flag = !isPowerOfTwo(NO_OF_ELEMENTS_IN_SET);
                    if(flag){
                        System.out.println("Error : Number of elements in a set should be a power of two.");
                    }
                    else{
                        flag = false;
                    }
                }
                catch(InputMismatchException e) {
                    System.out.println("Error : " + e);
                    s.nextLine();
                    flag = true;
                }
            }while(flag);

            //FOR CUSTOM TEST CASE
//            int cacheSizeInKB = 8;
//            BLOCK_SIZE = 64;
//            NO_OF_ELEMENTS_IN_SET = 4;

            OFFSET_BITS = (int)log2(BLOCK_SIZE);
            TAG_BITS = WORD_LENGTH - OFFSET_BITS;
            int sizeOfTag = (int)Math.pow(2, log2(cacheSizeInKB) + 10 - log2(BLOCK_SIZE));
            INDEX_BITS = (int)log2(sizeOfTag / NO_OF_ELEMENTS_IN_SET);
            sizeOfTag = sizeOfTag/NO_OF_ELEMENTS_IN_SET;
            tagArray = new ArrayList<ArrayList<String>>();
            dataArray = new ArrayList<ArrayList<Block>>();

            for(int i = 0; i < sizeOfTag; i++){
                tagArray.add(new ArrayList<String>());
                dataArray.add(new ArrayList<Block>());
            }

            //FOR CUSTOM TEST CASE
//            lookup("1000", false, "0101");
//            lookup("2000", false, "0001");
//            lookup("3000", false, "1001");
//            lookup("4000", false, "1101");

            flag = false;
            //ACTUAL RUNNER
            do{
                try{
                    System.out.println("Enter the function that you want to perform(read, write, print, exit) : ");
                    String mode = s.next();
                    mode = mode.trim();
                    mode = mode.toLowerCase();
                    flag = true;
                    //READ MODE
                    if(mode.equals("read")){
                        boolean insideFlag = false;
                        do{
                            try{
                                System.out.println("Enter address in hexadecimal : ");
                                String h = s.next();
                                if(h.substring(0,2).equals("0x") || h.substring(0,2).equals("0X")){
                                    h = h.substring(2);
                                }
                                if(h.length() > 8){
                                    System.out.println("Error : The address should be 32 bit. ");
                                    insideFlag = true;
                                }
                                else if(!isHex(h)){
                                    System.out.println("Error : Address is not in hexadecimal format!");
                                    insideFlag = true;
                                }
                                else{
                                    lookup(h, true, "NULLNULL");
                                    insideFlag = false;
                                }
                            }
                            catch(InputMismatchException e){
                                System.out.println("Error : " + e);
                                insideFlag = true;
                                s.nextLine();
                            }
                        }while(insideFlag);
                    }
                    //WRITE MODE
                    else if(mode.equals("write")){
                        boolean insideFlag = false;
                        do{
                            try{
                                System.out.println("Enter address in hexadecimal and data in Binary: ");
                                String h = s.next();
                                String data = s.next();
                                //HEXADECIMAL VALIDATION
                                if(h.substring(0,2).equals("0x") || h.substring(0,2).equals("0X")){
                                    h = h.substring(2);
                                }
                                if(h.length() > 8){
                                    System.out.println("Error : The address should be 32 bit. ");
                                    insideFlag = true;
                                }
                                else if(!isHex(h)){
                                    System.out.println("Error : Address should be in hexadecimal format!");
                                    insideFlag = true;
                                }
                                //DATA VALIDATION
                                else if(data.length() > 8){
                                    System.out.println("Error : Data cannot be more than 8 bits.");
                                    insideFlag = true;
                                }
                                else if(!isBinary(data)){
                                    System.out.println("Error : Data should be in binary format.");
                                    insideFlag = true;
                                }
                                else{
                                    lookup(h, false, data);
                                    insideFlag = false;
                                }
                            }
                            catch(InputMismatchException e){
                                System.out.println("Error : " + e);
                                insideFlag = true;
                                s.nextLine();
                            }
                        }while(insideFlag);
                    }
                    //PRINT MODE
                    else if(mode.equals("print")){
                        printCache();
                    }
                    //EXIT
                    else if(mode.equals("exit")){
                        flag = false;
                    }
                    //MAIN MEMORY
                    else if(mode.equals("main")){
                        printMainMemory();
                    }
                    //INVALID
                    else{
                        System.out.println("Error : Invalid Input.");
                        flag = true;
                    }
                }
                catch(InputMismatchException e){
                    System.out.println("Error : " + e);
                    s.nextLine();
                    flag = true;
                }
            }while(flag);
        }
    }

    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);
        boolean flag = false;
        do{
            try{
                System.out.println("Enter 1 for Direct Mapped Cache, 2 for Fully Associative Cache and 3 for n-way Set Associative Cache: ");
                int x = s.nextInt();
                if(x == 1){
                    directMapped cache = new directMapped();
                    cache.directRunner(s);
                    flag = false;
                }
                else if(x == 2){
                    fullyAssociative cache = new fullyAssociative();
                    cache.fullyRunner(s);
                    flag = false;
                }
                else if(x == 3){
                    nWayAssociative cache = new nWayAssociative();
                    cache.nWayRunner(s);
                    flag = false;
                }
                else{
                    System.out.println("Error : Invalid Input!");
                    flag = true;
                }
            }
            catch(InputMismatchException e){
                s.nextLine();
                System.out.println("Error : " + e);
                flag = true;
            }
        }while(flag);
    }

}
