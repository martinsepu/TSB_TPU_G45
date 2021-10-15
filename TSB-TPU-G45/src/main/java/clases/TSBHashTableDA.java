package clases;

import java.util.*;

public class TSBHashTableDA<K,V> extends AbstractMap<K,V> implements Cloneable {
    private final static int MAX_SIZE = Integer.MAX_VALUE;
    private Entry<K, V> table[];
    private int cantidad;
    private float load_factor;
    private int capacidad_inicial;
    private int modificaciones;

    private transient Set<K> keySet = null;
    private transient Set<Map.Entry<K,V>> entrySet = null;
    private transient Collection<V> values = null;

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

    public int hashCode(){
        int hc= (this.cantidad+super.hashCode())%table.length;
        return hc;
    }

    @Override
    public void clear() {
        table = new Entry[capacidad_inicial];
        cantidad = 0;
        this.modificaciones++;
    }

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
                    this.table[(clave + i * i) % table.length].setValue(value);
                    encontre=true;
                }
                i++;
            }while (!encontre);
            return (V) actual.getValue();
        }
    }


    public void putall(AbstractMap<? extends K, ? extends V> m){
        for (Map.Entry<? extends  K, ? extends V> e : m.entrySet()){
            put(e.getKey(),e.getValue());
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

    @Override
    public boolean containsValue(Object value) {
        if(value == null) return false;
        for(int i = 0; i < table.length; i++){
            if(table[i] != null && !table[i].isBorrado() && table[i].getValue() == value) return true;
        }
        return false;
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


    public V get(Object key) {
        int clave = h((K) key);
        int i = 0;
        V ans = null;
        while(table[(clave+i*i) % table.length] != null){
            if(table[(clave+i*i) % table.length].getKey() == key){
                ans = table[(clave+i*i) % table.length].getValue();
                return ans;
            }
            i++;
        }
        return ans;
    }

    public V remove(Object key){
        if(key == null) throw new NullPointerException("remove(): parámetro null");

        int clave=this.h((K) key), i = 0;
        Entry actual=null;
        do{
            actual = this.table[(clave + i * i) % table.length];
            if (actual==null){
                return null;
            }
            if(actual.getKey()==key){
                this.table[(clave + i * i) % table.length].borrar();
                cantidad--;
                modificaciones++;
                return (V) actual.getValue();
            }
            i++;
        }while (actual!=null);
        return null;
    }


    public Object clone() throws CloneNotSupportedException{
        TSBHashTableDA<K,V> t = (TSBHashTableDA<K, V>) super.clone();
        t.table = new Entry[table.length];
        for(int i=0;i<table.length;i++){
            t.table[i]=this.table[i];
        }
        t.keySet=null;
        t.entrySet = null;
        t.values=null;
        t.modificaciones=0;
        return t;
    }

    public boolean equals(Object obj){
        if(!(obj instanceof Map)) return false;

        Map<K, V> t = (Map<K, V>) obj;
        boolean iguales =true;
        if(t.size() != this.size())return false;

        Iterator<Map.Entry<K,V>> i = this.entrySet().iterator();
        Iterator<Map.Entry<K,V>> tem = t.entrySet().iterator();
        while (i.hasNext() && tem.hasNext()){
            Map.Entry<K, V> e = i.next();
            Map.Entry<K,V> s = tem.next();
            if(!e.equals(s)) return false;
        }
        if (i.hasNext()!=tem.hasNext())return false;
        return true;
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

    @Override
    public Set<K> keySet() {
        if(keySet == null){
            keySet = new KeySet();
        }
        return keySet;
    }

    @Override
    public Collection<V> values() {
        if(values == null){
            values = new ValueCollection();
        }
        return values;
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        if(entrySet == null){
            entrySet = new EntrySet();
        }
        return entrySet;
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

        @Override
        public boolean contains(Object o) {
            return TSBHashTableDA.this.containsKey(o);
        }

        public boolean remove(Object o){ return (TSBHashTableDA.this.remove(o)!=null);}


        private class KeySetIterator implements Iterator<K>{
            private int cont, current, expected_modCount;
            public KeySetIterator(){
                cont = 0;
                current = -1;
                expected_modCount= TSBHashTableDA.this.modificaciones;
            }
            @Override
            public boolean hasNext() {
                return (cont < size());
            }

            @Override
            public K next() {
                if(TSBHashTableDA.this.modificaciones != expected_modCount)
                {
                    throw new ConcurrentModificationException("next(): modificación inesperada de tabla...");
                }

                if(!hasNext())
                {
                    throw new NoSuchElementException("next(): no existe el elemento pedido...");
                }

                Entry<K,V> t[] = TSBHashTableDA.this.table;
                while(t[current] == null || t[current].isBorrado()) current++;
                K ans = t[current].getKey();
                current++;
                cont++;
                return ans;
            }

        }
    }
    private class ValueCollection extends AbstractCollection<V>{

        @Override
        public Iterator iterator() {
            return new ValueCollectionIterator();
        }

        @Override
        public int size() {
            return TSBHashTableDA.this.size();
        }

        @Override
        public boolean contains(Object o) {
            return TSBHashTableDA.this.containsValue(o);
        }

        @Override
        public void clear() {
            TSBHashTableDA.this.clear();
        }

        private class ValueCollectionIterator implements Iterator<V>{
            private int current, cont;
            public ValueCollectionIterator(){
                current = 0;
                cont = 0;
            }
            @Override
            public boolean hasNext() {
                return (cont < TSBHashTableDA.this.size());
            }

            @Override
            public V next() {
                if(!hasNext())
                {
                    throw new NoSuchElementException("next(): no existe el elemento pedido...");
                }
                Entry<K,V> t[] = TSBHashTableDA.this.table;
                while(t[current] == null || t[current].isBorrado()) current++;
                V ans = t[current].getValue();
                current++;
                cont++;
                return ans;
            }
        }
    }
    private class EntrySet extends AbstractSet<Map.Entry<K,V>>{

        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            return new EntrySetIterator();
        }

        @Override
        public int size() {
            return TSBHashTableDA.this.size();
        }
        private class EntrySetIterator implements Iterator<Map.Entry<K,V>>{
            private int current, cont;
            public EntrySetIterator(){
                current = 0;
                cont = 0;
            }
            @Override
            public boolean hasNext() {
                return (cont < TSBHashTableDA.this.size());
            }

            public boolean contains(Object o){
                Entry entrada= (Entry) o;
                K clave = (K)entrada.getKey();
                return TSBHashTableDA.this.containsKey(clave);
            }

            @Override
            public Entry<K,V> next() {
                if(!hasNext())
                {
                    throw new NoSuchElementException("next(): no existe el elemento pedido...");
                }
                Entry<K,V> t[] = TSBHashTableDA.this.table;
                while(t[current] == null || t[current].isBorrado()) current++;
                Entry<K,V> ans = t[current];
                current++;
                cont++;
                return ans;
            }
        }
    }
}
