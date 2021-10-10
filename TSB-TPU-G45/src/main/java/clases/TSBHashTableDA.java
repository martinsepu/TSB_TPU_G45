package clases;

import java.util.*;

public class TSBHashTableDA<K,V> extends AbstractMap<K,V> implements Cloneable {
    private final static int MAX_SIZE = Integer.MAX_VALUE;
    private Entry<K, V> table[];
    private int cantidad;
    private float load_factor;
    private int capacidad_inicial;
    private int modificaciones;


    public TSBHashTableDA(){this(11,0.8f);}

    public TSBHashTableDA(int capacidad){this(capacidad,0.8f);}

    public TSBHashTableDA(int capacidad,float load_factor){
        if(load_factor<=0 || load_factor>=1) load_factor=0.8f;
        if(capacidad<=0) capacidad=11;
        else {
            if (capacidad>TSBHashTableDA.MAX_SIZE) capacidad=TSBHashTableDA.MAX_SIZE;
        }

        this.table= new Entry[capacidad];
        this.cantidad=0;
        this.capacidad_inicial=capacidad;
        this.load_factor=load_factor;
        modificaciones=0;
    }

    @Override
    public int size() {return cantidad;}

    public boolean isEmpty(){
        if (cantidad==0)return true;
        return false;
    }

    public V put(K key,V value) {

        if (key == null || value == null) throw new NullPointerException("put(): parámetro null");

        int clave = h(key);
        int i = 0;
        V valorprevio = null;
        boolean encontre=false;
        Entry actual = null;
        Entry<K, V> entrada = new Entry<K, V>(key, value);

        if (table[clave] == null) {
            if(cantidad>=this.load_factor*this.table.length){
                this.rehash();
                return (put(key,value));
            }
            this.table[clave] = entrada;
            cantidad++;
            modificaciones++;
            return null;
        }

        if(!containsKey(key)){
            if(cantidad>=this.load_factor*this.table.length) {
                this.rehash();
                clave=h(key);
            }
            do {
                actual = this.table[(clave + i * i) % table.length];
                if(actual==null || actual.isBorrado()) {
                    this.table[(clave + i * i) % table.length] = entrada;
                    encontre=true;
                }
                i++;
            }while (!encontre);
            cantidad++;
            modificaciones++;
            return valorprevio;
        }
        else {
            do {
                actual=this.table[(clave + i * i) % table.length];
                if(actual.getKey() == key){
                    this.table[(clave + i * i) % table.length] = entrada;
                    encontre=true;
                }
                i++;
            }while (!encontre);
            return (V) actual.getValue();
        }
    }

    protected void rehash(){
        int longitud_previa=this.table.length;
        int longitud_nueva= longitud_previa *2 +1;

        if(longitud_nueva > TSBHashTableDA.MAX_SIZE)
        {
            longitud_nueva = TSBHashTableDA.MAX_SIZE;
        }

        Entry<K,V> temp[]=this.table;
        this.table= new Entry[longitud_nueva];

        for(int i=0;i<longitud_previa;i++){
            if(temp[i]!=null){
                if (temp[i].isBorrado()) continue;
                else {
                    this.put(temp[i].getKey(),temp[i].getValue());
                }
            }
        }
        modificaciones++;
    }

    public boolean contains(Object value){
        if(value == null) return false;


        return true;
    }

    public boolean containsKey (Object key){
        if(key == null) return false;

        int clave = h((K)key), i = 0;
        Entry actual=null;
        boolean noEncontre=true;

        do{
            actual = this.table[(clave + i * i) % table.length];
            if (actual==null)return false;
            if (!actual.isBorrado()){
                if (actual.getKey() == (K) key) noEncontre = false;
            }
            i++;
        }while (actual!=null && noEncontre);

        return !noEncontre;
    }

    public V remove(Object key){
        if(key == null) throw new NullPointerException("remove(): parámetro null");

        int clave=this.h((K) key), i = 0;
        Entry actual=null;
        do{
            actual = this.table[(clave + i * i) % table.length];
            if (actual==null)return null;
            if(actual.getKey()==key){
                this.table[(clave + i * i) % table.length].borrar();
                return (V) actual.getValue();
            }
            i++;
        }while (actual!=null);
        return null;
    }

    @Override
    public Set<AbstractMap.Entry<K, V>> entrySet() {
        return null;
    }

    public Object clone(){
        return null;
    }

    /**
     * calculo de la clave de hash de una key
     * @param k
     * @return el nuemero entero obtenido al realizarle la funcioon hash a la clave
     */
    private int h(int k){return h(k, this.table.length);}
    private int h(K key){return h(key.hashCode(), this.table.length);}
    private int h(K key, int t){return h(key.hashCode(),t);}
    private int h(int k, int t){
        if(k < 0) k *=-1;
        return k %t;
    }


    private class Entry<K,V> implements AbstractMap.Entry<K,V>{
        private K key;
        private V value;
        private boolean borrado;

        public Entry(K key, V value){
            if(key ==null || value == null){
                throw new IllegalArgumentException("Entry(): parámetro null...");
            }
            this.key = key;
            this.value = value;
            this.borrado=false;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        public boolean isBorrado() {
            return borrado;
        }

        @Override
        public V setValue(V value) {
            if(value!=null) this.value=value;
            V viejo = this.value;
            this.value = value;
            return viejo;
        }

        public boolean equals(Object obj){
            if (this == obj) { return true; }
            if (obj == null) { return false; }
            if (this.getClass() != obj.getClass()) { return false; }

            final Entry other = (Entry) obj;
            if (!Objects.equals(this.key, other.key)) { return false; }
            if (!Objects.equals(this.value, other.value)) { return false; }
            return true;
        }

        public int hashCode(){
            int hash = 7;
            hash = 61 * hash + Objects.hashCode(this.key);
            hash = 61 * hash + Objects.hashCode(this.value);
            return hash;
        }
        public void borrar(){
            this.borrado=true;
        }
    }

    private class KeySet extends AbstractSet<K>{

        @Override
        public Iterator<K> iterator() {
            return new KeySetIterator();
        }

        @Override
        public int size() {
            return TSBHashTableDA.this.cantidad;
        }

        private class KeySetIterator implements Iterator<K>{


            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public K next() {
                return null;
            }

            @Override
            public void remove() {
                Iterator.super.remove();
            }
        }
    }
}
