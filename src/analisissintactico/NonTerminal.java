/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analisissintactico;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Pedro_Acevedo
 */
public class NonTerminal {
    
    private String letra;
    private ArrayList<String> producciones;
    private HashMap<String,String> primPerProd;
    private ArrayList<String> PRIMERO;
    private ArrayList<String> SIGUIENTE;
    private String sequence;
    
    public NonTerminal(String letra) {
        this.letra = letra;
        this.sequence="";
        this.producciones = new ArrayList();
        this.PRIMERO = new ArrayList();
        this.SIGUIENTE = new ArrayList();
        this.primPerProd = new HashMap();
    }
    
    public void addProduccion(String p){
       this.producciones.add(p);
    }
    
    public boolean isRecursive(){
        return producciones.stream().anyMatch((produccion) -> (produccion.startsWith(letra)));
    }
    
    public boolean isFact(){
        for (int i = 0; i < producciones.size(); i++) {
            for (int j = producciones.get(i).length()-1; j >= 1; j--) {
                String seq = producciones.get(i).substring(0,j);
                int rep = 0;
                for (int k = 0; k < producciones.size(); k++) {
                    if (i!=k) {
                        if (producciones.get(k).startsWith(seq)) {
                            rep++;
                        }
                    }
                }
                if (rep>0) {
                    sequence = seq;
                    return true;
                }
            }
        }
        return false;
    }
    
    public void Comprueba(){
        if (isRecursive()){
            String alfa = "",beta = "";
            for (int i = 0; i < producciones.size(); i++) {
                if (producciones.get(i).startsWith(letra)) {
                   alfa = alfa + producciones.get(i).substring(1) + ";";
                }else{
                   beta = beta + producciones.get(i) + ";";
                }
            }
            producciones = new ArrayList();
            String[] a = beta.split(";");
            for (String p : a) {
                producciones.add(p+letra+"'");
            }
            int pos = Inicio.NoTerminales.indexOf(this)+1;
            Inicio.NoTerminales.add(pos,new NonTerminal(letra+"'"));
            Inicio.setNoTerminales.add(pos,letra+"'");
            a = alfa.split(";");
            for (String p : a) {
                Inicio.NoTerminales.get(pos).addProduccion(p+letra+"'");
            }
            Inicio.NoTerminales.get(pos).addProduccion("&");
        }else{  
            if (isFact()) {
                String beta = "";
                ArrayList<String> remove = new ArrayList<>();
                for (String produccion : producciones) {
                    if (produccion.startsWith(sequence)) {
                        remove.add(produccion);
                        if (produccion.equals(sequence)) {
                            beta = beta + "&" + ";";
                        }else{
                            beta = beta + produccion.substring(sequence.length()) + ";";                        
                        }
                    }
                }
                producciones.removeAll(remove);
                producciones.add(0,sequence+letra+"'");
                int pos = Inicio.NoTerminales.indexOf(this)+1;
                Inicio.NoTerminales.add(pos,new NonTerminal(letra+"'"));
                Inicio.setNoTerminales.add(pos,letra+"'");
                String[] a = beta.split(";");
                for(String p:a){
                    Inicio.NoTerminales.get(pos).addProduccion(p);
                }
                
            }
        }
    }
    String pProd = "";
    public void PRIMERO(){
        if (PRIMERO.isEmpty()) {
            for(String produccion: producciones){
                pProd = "";
                Pattern patron = Pattern.compile("^[A-Z]");
                Matcher coincide = patron.matcher(produccion);
                if (coincide.find()) {
                    int pos = Index(produccion.substring(0,1));
                    System.out.println("HOLA SOY" + letra + "->" + produccion);
                    Inicio.NoTerminales.get(pos).getPRIMEROA().forEach((p)->{if(!PRIMERO.contains(p))PRIMERO.add(p);pProd=pProd+p+"<!!!>";});
                    if (Inicio.NoTerminales.get(pos).isEmpty()){
                        int i = 0;
                        boolean sw = false;
                        while ( !sw &&i < Inicio.Terminales.size()) {
                            String prod = produccion.substring(1);
                            if (prod.startsWith(Inicio.Terminales.get(i))) {
                                sw = true;
                                String t = Inicio.Terminales.get(i);
                                if (!PRIMERO.contains(t)){PRIMERO.add(t);pProd=pProd+t+"<!!!>";}
                            }
                            i++;
                        }
                        if(!sw){
                            int j = 1;
                            boolean end = false;
                            while(!end && j<produccion.length()){
                                int c = Index(produccion.substring(j,j+1));
                                Inicio.NoTerminales.get(c).getPRIMEROA().forEach((p)->{if(!PRIMERO.contains(p))PRIMERO.add(p);pProd=pProd+p+"<!!!>";});
                                if (Inicio.NoTerminales.get(c).isEmpty()){
                                    i = 0;
                                    sw = false;
                                    while ( !sw &&i < Inicio.Terminales.size()) {
                                        String prod = produccion.substring(j);
                                        if (prod.startsWith(Inicio.Terminales.get(i))) {
                                            sw = true;
                                            end = true;
                                            String t = Inicio.Terminales.get(i);
                                            if (!PRIMERO.contains(t)){PRIMERO.add(t);pProd=pProd+t+"<!!!>";}
                                        }
                                            i++;
                                    }
                                    if (!sw) {
                                        j++;
                                    }
                                }else{
                                   end = true;
                                }
                            }
                            if (!end) {
                                PRIMERO.add(Inicio.epsilon);
                                pProd=pProd+Inicio.epsilon+"<!!!>";
                            }
                        }
                    }    
                }else{
                    String t = produccion.substring(0, 1);
                    if (!PRIMERO.contains(t)){PRIMERO.add(t);pProd=pProd+t+"<!!!>";}
                    
                }
            primPerProd.put(produccion, pProd);
            }
        }    
    }
    
    public ArrayList<String> PRIMERO(String p){
        String [] pp = primPerProd.get(p).split("<!!!>");
        ArrayList<String> prim = new ArrayList<>();
        prim.addAll(Arrays.asList(pp));
        return prim;
    }
   
    public void SIGUIENTE(){
        if (isInicial()) {
            SIGUIENTE.add("$");
        }
        HashMap c = isIN();
        Iterator<String> noT = c.keySet().iterator();
        while(noT.hasNext()){
            //System.out.println("Entre " + letra);
           String nt = noT.next();
           //System.out.println("La de este cole " + letra + " -->" + nt);
           String[] sp = c.get(nt).toString().split("<!!!>");
            for (String prod : sp) {
                //System.out.println(prod);
                ArrayList<Integer> e = whatPos(prod);
                //System.out.println(prod);
                for (int i :e) {
                    //System.out.println(prod.length() + "--" + i + "----" + (prod.length()-1-(letra.length()-1)) + letra );
                    if ((prod.length()-1-(letra.length()-1)) == i) {
                        if(!nt.equals(letra)){    
                            int pos = Index(nt);
                            if (Inicio.NoTerminales.get(pos).SIGUIENTE.isEmpty()) {
                                Inicio.NoTerminales.get(pos).SIGUIENTE();
                            }
                            Inicio.NoTerminales.get(pos).getSIGUIENTEA().forEach((p)->{if(!SIGUIENTE.contains(p))SIGUIENTE.add(p);});
                        }
                    }else{
                        int k = 0;
                        boolean sw = false;
                        while ( !sw && k < Inicio.Terminales.size()) {
                            String prods = prod.substring(i+1);
                            if (prods.startsWith(Inicio.Terminales.get(k))) {
                                sw = true;
                                String t = Inicio.Terminales.get(k);
                                if(!SIGUIENTE.contains(t)){SIGUIENTE.add(t);};
                            }
                            k++;
                        }
                        int j = i+1;
                        while(!sw && j < prod.length()){
                            System.out.println(letra + "--" + j + "--" + prod );
                            int c1 = Index(whatNT(prod.substring(j)));
                            Inicio.NoTerminales.get(c1).getPRIMEROA().forEach((p)->{if(!SIGUIENTE.contains(p)){SIGUIENTE.add(p);}});
                            if (Inicio.NoTerminales.get(c1).isEmpty()){
                                System.out.println(letra + "--" + j + "--" + prod );
                                if (j==(prod.length()-1)) {
                                    int c2 = Index(whatNT(nt));
                                    Inicio.NoTerminales.get(c2).getSIGUIENTEA().forEach((p)->{if(!SIGUIENTE.contains(p))SIGUIENTE.add(p);});
                                    sw=true;
                                }else{
                                    j++;
                                    k = 0;
                                    sw = false;
                                    if (j==(prod.length()-1) && prod.substring(j).equals("'")) {
                                        int c2 = Index(whatNT(nt));
                                        Inicio.NoTerminales.get(c2).getSIGUIENTEA().forEach((p)->{if(!SIGUIENTE.contains(p)){SIGUIENTE.add(p);}});
                                        sw=true;
                                    }
                                    while ( !sw && k < Inicio.Terminales.size()) {
                                        String prods = prod.substring(j);
                                        if (prods.startsWith(Inicio.Terminales.get(k))) {
                                            sw = true;
                                            String t = Inicio.Terminales.get(k);
                                            if(!SIGUIENTE.contains(t)){SIGUIENTE.add(t);}
                                        }
                                        k++;
                                    }
                                }    
                            }else{
                                sw = true;
                            }   
                        }
                    }
                }
            }
        }        
    }
    
    public String whatNT(String p){
        for (int i = 0; i < Inicio.NoTerminales.size(); i++) {
        if (p.startsWith(Inicio.NoTerminales.get(i).letra)) {
                if (Inicio.NoTerminales.get(i).letra.length()==1 && p.length()>1) {
                    if (p.substring(1,2).equals("'")) {
                        return Inicio.NoTerminales.get(i).letra+"'";
                    }else{
                        return Inicio.NoTerminales.get(i).letra;
                    }
                }else{
                     return Inicio.NoTerminales.get(i).letra;
                }
            }
        }
        return "";
    }
    
    public ArrayList<Integer> whatPos(String p){
        ArrayList<Integer> rep = new ArrayList<>();
        for (int i = 0; i < p.length()-(letra.length()-1); i++) {
            if (p.substring(i, i+letra.length()).equals(letra)) {
                if (letra.length()==1) {
                    if ( (i+1) < p.length()){
                        if (!p.substring(i+1, i+2).equals("'")) {
                            rep.add(i);
                        }
                    }else{
                         rep.add(i);
                    }
                }else{
                    rep.add(i);
                }
            }
        }
        return rep;
    }
    
    public boolean contains(String s){
        boolean sw = s.contains(letra); 
        if (sw && letra.length()==1) {
            for (int i = 0; i < s.length(); i++) {
                if (s.substring(i,i+1).equals(letra)) {
                    if ((i+1) < s.length()) {
                        if (s.substring(i+1,i+2).equals("'")) {
                            return false;
                        }else{
                            return true;
                        }
                    }
                }
            }
        }
        return sw;
    }
    
    public boolean contains(String s,String l){
        boolean sw = s.contains(l); 
        if (sw && l.length()==1) {
            for (int i = 0; i < s.length(); i++) {
                if (s.substring(i,i+1).equals(l)) {
                    if ((i+1) < s.length()) {
                        if (s.substring(i+1,i+2).equals("'")) {
                            return false;
                        }else{
                            return true;
                        }
                    }
                }
            }
        }
        return sw;
    }
      
    public HashMap<String,String> isIN(){
        HashMap<String,String> has = new HashMap<>();
        for (NonTerminal NoTerminal : Inicio.NoTerminales) {
            String p = "";
            for (String produccion : NoTerminal.producciones) {
                if (contains(produccion)){
                    p = p + produccion +"<!!!>";
                }
            }
            if (!p.equals("")) {
                has.put(NoTerminal.letra, p);   
            }
        }
        return has;
    }
    
    public boolean isEmpty(){
        return PRIMERO.contains(Inicio.epsilon);
    }

    public boolean isInicial(){ 
        return Inicio.NoTerminales.get(0).equals(this);
    }

    public String getLetra() {
        return letra;
    }
    
    public String getPRIMERO() {
        if (PRIMERO.isEmpty()) {
            PRIMERO();
        }
        String T = letra + ":{";
        for (String primero : PRIMERO) {
            T = T + " " + primero + ",";       
        }
        T = T.substring(0, T.length()-1) + " }";
        return T;
    }

    public ArrayList<String> getPRIMEROA() {
        if (PRIMERO.isEmpty()) {
            PRIMERO();
        }
        ArrayList<String> prim = new ArrayList<>();
        PRIMERO.forEach((p)->{if (!p.equals(Inicio.epsilon)){prim.add(p);}});
        return prim;
    }

   
    public String getSIGUIENTE() {
        if (SIGUIENTE.isEmpty()) {
            SIGUIENTE();
        }
        String T = letra + ":{";
        for (String siguiente : SIGUIENTE) {
            T = T + " " + siguiente + ",";
        }
        T = T.substring(0, T.length()-1)  + " }";
        return T;
    }

    public ArrayList<String> getProducciones() {
        return producciones;
    }
    
    public ArrayList<String> getSIGUIENTEA() {
        if (SIGUIENTE.isEmpty()) {
            SIGUIENTE();
        }
        return SIGUIENTE;
    }      
    
    public int Index(String nt){
        for (int i = 0; i < Inicio.NoTerminales.size(); i++) {
            if (Inicio.NoTerminales.get(i).letra.equals(nt)) {
                return i;
            }
        }
        return 0;
    }
    
    @Override
    public String toString() {
        String T = "";
        for (String produccion : producciones) {
            T = T + letra + "->" + produccion + "\n";
        }
        return T.substring(0,T.length()-1);
    }
        
}
