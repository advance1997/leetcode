package simple;

public class PlusTable {

    public static void main(String[] args) {
        printPlusTable();
    }

    public static void printPlusTable(){
        for (int i = 1 ; i < 10 ; i++){
            for(int j = 1 ; j <= i ;j ++){
                System.out.print(j+"*"+i+"="+(i*j)+"\t");
            }
            System.out.println("");
        }
    }

}
