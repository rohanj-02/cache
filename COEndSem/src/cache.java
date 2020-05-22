import java.util.Scanner;
import java.util.ArrayList;

public class cache{

    static int WORD_LENGTH = 32;

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

        void printBlock(){
            for(int i = 1; i <= size; i++){
                data[i - 1].printMemory();
                if(i % 8 == 0){
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

    static class directMapped{

    }

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
                }
                mainMemory.remove(i);
            }

            Block blockTemp = dataArray.get(indexDecimal).get(NO_OF_ELEMENTS_IN_SET - 1);
            String tagTemp = tagArray.get(indexDecimal).get(NO_OF_ELEMENTS_IN_SET - 1) + makeNBit(Integer.toString(indexDecimal, 2), INDEX_BITS);
            BlockMem temp = new BlockMem(tagTemp, blockTemp);
            mainMemory.add(temp);
            System.out.println("Block Memory to be replaced: " + temp.tagPart);
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
                        writeCache(offsetDecimal, indexDecimal, i, value);
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
                    writeCache(offsetDecimal, indexDecimal, 0, value);
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
                    writeCache(offsetDecimal, indexDecimal, 0, value);
                }
            }
        }

        void printCache(){
            for(int i = 0; i < tagArray.size(); i++){
                System.out.println("Set Number : " + i);
                for(int j = 0; j < tagArray.get(i).size(); j++){
                    System.out.println("Block Address : " + tagArray.get(i).get(j) + makeNBit(Integer.toString(i, 2), INDEX_BITS));
                    dataArray.get(i).get(j).printBlock();
                }
                if(tagArray.get(i).size() == 0){
                    System.out.println("Empty Set");
                }
            }
        }

        void printMainMemory(){
            for(int i = 0; i < mainMemory.size(); i++){
                System.out.println("Tag : " + mainMemory.get(i).tagPart);
                mainMemory.get(i).block.printBlock();
            }
        }

        void nWayRunner(Scanner s){
            //INPUT
//        System.out.println("Enter size of cache in KB : ");
//        int cacheSizeInKB = s.nextInt();
//        System.out.println("Enter number of cache lines : ");
//        int cacheLine = s.nextInt();
//        System.out.println("Enter size of block in Bytes: ");
//        BLOCK_SIZE = s.nextInt();
//        System.out.println("Enter n for n-way set associative cache: ");
//        NO_OF_ELEMENTS_IN_SET = s.nextInt();

            int cacheSizeInKB = 8;
            BLOCK_SIZE = 64;
            NO_OF_ELEMENTS_IN_SET = 4;

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

            int mode = 1;

            lookup("1000", false, "0101");
            lookup("2000", false, "0001");
            lookup("3000", false, "1001");
            lookup("4000", false, "1101");


            while(mode == 1 || mode == 2 || mode == 3 || mode == 4){
                System.out.println("Enter 1 for Reading, 2 for writing and 3 for seeing the cache : ");
                mode = s.nextInt();
                switch(mode){
                    case 1:
                        System.out.println("Enter address in hexadecimal : ");
                        lookup(s.next(), true, "NULLNULL");
                        break;
                    case 2:
                        System.out.println("Enter address(hexadecimal) and value(binary) to store : ");
                        String add = s.next();
                        String data = s.next();
                        lookup(add, false, data);
                        break;
                    case 3:
                        printCache();
                        break;
                    case 4:
                        printMainMemory();
                        break;
                }
            }
        }

    }

    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);
        nWayAssociative cache = new nWayAssociative();
        cache.nWayRunner(s);
    }

}