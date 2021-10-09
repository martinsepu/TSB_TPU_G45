package clases;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class TSBHashTableDA<K,V> extends AbstractMap<K,V> implements Cloneable {
    private final static int MAX_SIZE = Integer.MAX_VALUE;
    private Entry<K, V> table[];
    private int cantida;
    private float load_factor;
    private int modificaciones;

    public TSBHashTableDA(){
        this.table= new Entry[6];
        cantida=0;
        load_factor=0.8f;
        modificaciones=0;

    }


    public V put(K key,V value){

        if(key == null || value == null) throw new NullPointerException("put(): parámetro null");

        int clave = h(key);
        K keyAnt=null;
        V valorprevio=null;
        Entry<K, V> entrada = new Entry<K, V>(key, value);

        if(table[clave]==null) {
            this.table[clave] = entrada;
            cantida++;

        }
        else {
            keyAnt = table[clave].getKey();
            if (keyAnt==key) {
                valorprevio = table[clave].getValue();
                this.table[clave] = entrada;

            }
            else {
                if(this.cantida>=this.load_factor*this.table.length) {
                    this.rehash();
                    clave = h(key);
                }
                for(int i=0;i<table.length;i++)
                    if (this.table[(clave + i*i)%table.length]==null)this.table[(clave + i*i)%table.length]=entrada;
                cantida++;
            }
        }
        modificaciones++;
        return valorprevio;

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

    @Override
    public int size() {
        return cantida;
    }

    @Override
    public Set<AbstractMap.Entry<K, V>> entrySet() {
        return null;
    }

    public Object clone() throws CloneNotSupportedException{
        TSBHashTableDA<K,V> t=(TSBHashTableDA<K, V>) super.clone();
        t.table=new Entry[table.length];


        return null;
    }

    private int h(int k){
        return h(k, this.table.length);
    }
    private int h(K key){

        return h(key.hashCode(), this.table.length);

    }
    private int h(K key, int t){
        return h(key.hashCode(),t);
    }
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


    }
}
