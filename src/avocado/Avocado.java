
package avocado;

import avocado.controllers.MainController;

/**
 * Avocado main class
 * @author Mario
 */
public class Avocado {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println(Avocado.icon);
        MainController main = new MainController();
    }
    
    // <editor-fold defaultstate="collapsed" desc="Icon">
    static String icon = "" +
"----------------------------------------\n" +
"|          Welcome to AVOCADO          |\n" +
"----------------------------------------\n" +
"              ...                       \n" +
"      .....OOOOOOOOO.                   \n" +
"  ..++~~$OOOOOOOOOOOOOO..               \n" +
" ..+~~~+OOOOOOOOOOOOOOOOO....           \n" +
"..+~~~~OOOOOOOOOOOOOOOOO~~~~++....      \n" +
"..+~~~~?OOOOOOOOOOOOOOOO~~~~~~~~++..    \n" +
"....+~~~+Z.DOOOOOOOOOOO8~~~~~~~~~~~+... \n" +
"......++~~~~~?$O..DDDZ?~~~~~~~~~~~~~~+..\n" +
"..........+++~~~~~~~~~~~~~~~~~~~~~~~++..\n" +
"................++++++++++++++++++..... \n" +
" .....................................  \n" +
"  ...................................   \n" +
"     ............................       \n" +
"        ......................          \n" +
"             ...........                ";
    // </editor-fold>
    
}
