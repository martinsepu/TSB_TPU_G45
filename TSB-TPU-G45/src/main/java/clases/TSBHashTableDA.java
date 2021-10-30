package clases;

import java.util.*;

/**
 * Clase para emular la funcionalidad de la clase java.util.Hashtable provista
 * en forma nativa por Java. Una TSBHashTableDA usa un arreglo donde guarda todos los pares
 * de valores y claves de forma que si existe una colisión, busque la próxima posible posición
 * en la tabla de forma cuadrática usando direccionamiento abierto.
 * @author G45 - Francisco Savala-86146 - Martín Sepúlveda-78284 - Federico Verón-78713
 * @version 1.0
 * @param <K> tipo de key
 * @param <V> tipo de value
 */
public class TSBHashTableDA<K,V> extends AbstractMap<K,V> implements Cloneable {
    //tamaño maximo de la tabla
    private final static int MAX_SIZE = Integer.MAX_VALUE;
    //tabla de hash: arreglo que contiene los pares clave valor (feinidos como Entry)
    private Entry<K, V> table[];
    //cantidad de objetos que tiene toda la tabla
    private int cantidad;
    //factor de carga que indicara cuando hace falta ahcer un rehash
    private float load_factor;
    //tamaño inicial de la tabla
    private int capacidad_inicial;
    // contador de operaciones que cambian la tabla
    protected transient int modificaciones;
    //codigo hash propio de toda la tabla
    private int codigoHash;

    private transient Set<K> keySet = null;
    private transient Set<Map.Entry<K,V>> entrySet = null;
    private transient Collection<V> values = null;

    /**
     * Crea un objeto por defecto
     */
    public TSBHashTableDA(){this(11,0.8f);}

    /**
     * Crea un objeto solo con la capacidad.
     * @param capacidad
     */
    public TSBHashTableDA(int capacidad){this(capacidad,0.8f);}

    /**
     * Crea un objeto con los dos valores pasados por parametro
     * @param capacidad  capacidad inicial de la tabla
     * @param load_factor factor de llenado para rehash
     */
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
        codigoHash=0;
    }

    /**
     * Retorna la cantidad de elementos en la HashTable
     * @return un entero que indica la cantidad
     */
    @Override
    public int size() {return cantidad;}

    /**
     * Devuelve el código hash de la tabla
     * @return codigoHash: codigo hash de la tabla
     */
    public int hashCode(){
        //int hc = super.hashCode();
        //int hc= (this.cantidad+super.hashCode())%table.length;
        //int hash = 7;
        //hash = 61 * hash + Objects.hashCode(this.table);
        return codigoHash;
    }

    /**
     * Borra todos los elementos de la tabla y pone la cantidad en 0,
     * aumentando el umero de modificaciones
     */
    @Override
    public void clear() {
        table = new Entry[capacidad_inicial];
        cantidad = 0;
        this.modificaciones++;
    }

    /**
     * Permite saber si la tabla está vacía.
     * @return Un booleano que indica si está vacía la tabla de hash
     */
    public boolean isEmpty(){
        if (cantidad==0)return true;
        return false;
    }

    /**
     * Inserta un elemento (clave valor) en la tabla, si se supera el factor de carga se llama al metodo rehash,
     * al agregar un elemento modifica el código de hash de toda la tabla
     * @param key clave del elemento a insertar
     * @param value valor del elemento a insertar
     * @return: el valor del objeto que estaba en la posición donde se inserto el nuevo elemento
     * @throws java.lang.NullPointerException si algunos de los objetos que forman el objeto
     * clave valor que se quiere ingresar es un Null
     */
    public V put(K key,V value) {
        if(key == null || value == null) throw new NullPointerException("put(): parámetro null");

        int id = h(key);
        int sum = 0;
        V old = null;
        boolean encontre_espacio=false,piso=false;
        int lugar_espacio=0;
        int posicion_actual;

        do {
            posicion_actual=(id+sum*sum)%table.length;
            if (table[posicion_actual]!=null && table[posicion_actual].getKey()==key){
                piso=true;
            }
            if (table[posicion_actual]!=null && table[posicion_actual].isBorrado() && !encontre_espacio){
                encontre_espacio=true;
                lugar_espacio=posicion_actual;
            }
            sum++;
        }while (table[posicion_actual]!=null && !piso);

        if (piso){
            old=table[posicion_actual].getValue();
            codigoHash-=table[posicion_actual].hashCode();
            table[posicion_actual].setValue(value);
            codigoHash+=table[posicion_actual].hashCode();
            modificaciones++;
            return old;
        }
        if (encontre_espacio){
            old=null;
            table[lugar_espacio] = new Entry<>(key, value);
            codigoHash+=table[lugar_espacio].hashCode();
            cantidad++;
            modificaciones++;
            if(this.size()> table.length*load_factor) {
                this.rehash();
            }
            return old;
        }
        table[posicion_actual] = new Entry<>(key, value);
        codigoHash+=table[posicion_actual].hashCode();
        cantidad++;
        modificaciones++;
        if(this.size()> table.length*load_factor) {
            this.rehash();
        }
        return old;



    }
    /**
     * Inserta todos los elementos de un mapa en la tabla hash llamando para cada uno al metodo put
     * @param m mapa donde se encuentran los elementos a insertar
     */
    public void putall(AbstractMap<? extends K, ? extends V> m){
        for (Map.Entry<? extends  K, ? extends V> e : m.entrySet()){
            put(e.getKey(),e.getValue());
        }
    }

    /**
     * Aumenta la longitud de la tabla reubicando los elementos que se encontraban efectivamente en ella
     */
    protected void rehash(){
        int longitud_previa=this.table.length;
        int cantidad_previa = this.size();
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
        cantidad = cantidad_previa;
        modificaciones++;
    }

    /**
     * Permite conocer si alguna de las keys
     * tiene asociado el valor pasado por parámetro
     * @param value el valor que se quiere saber si existe.
     * @return retorna true si existe el valor
     */
    @Override
    public boolean containsValue(Object value) {
        if(value == null) return false;
        for(int i = 0; i < table.length; i++){
            if(table[i] != null && !table[i].isBorrado() && table[i].getValue() == value) return true;
        }
        return false;
    }

    /**
     * Permite conocer si existe la key en la hashtable
     * @param key la key que se quiere saber si existe.
     * @return returna true si existe la key
     */
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

    /**
     * Permite obtener el value al cual está asociado una key si esta existe.
     * @param key la key de la cual se quiere obtener el valor
     * @return el valor al que está asociado la key
     */
    public V get(Object key) {
        int clave = h((K) key);
        int i = 0;
        V ans = null;
        while(table[(clave+i*i) % table.length] != null){
            if(table[(clave+i*i) % table.length].getKey() == key){
                if(table[(clave+i*i) % table.length].isBorrado()) return null;
                ans = table[(clave+i*i) % table.length].getValue();
                return ans;
            }
            i++;
        }
        return ans;
    }

    /**
     * Elimina un elemento de la tabla marcandolo como borrado
     * @param key clave del elemento a borrar
     * @return valor del elemento borrado
     * @throws java.lang.NullPointerException si el objeto ingresado
     * como parametro es un Null
     */
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
                codigoHash=codigoHash-this.table[(clave + i * i) % table.length].hashCode();
                cantidad--;
                modificaciones++;
                return (V) actual.getValue();
            }
            i++;
        }while (actual!=null);
        return null;
    }

    /**
     * Retorna una copia superficial de la tabla, en los espacios de la nueva tabla
     * se almacean las direcciones de los mismos objetos que contiene la tabla original
     * @return copia superficial de la tablaa
     * @throws java.lang.CloneNotSupportedException si la clase no implementa la interfaz
     * clonable
     */
    public Object clone(){
        TSBHashTableDA<K, V> t;
        try {
            t = (TSBHashTableDA<K, V>) super.clone();
            t.table = new Entry[table.length];
            for (int i = 0; i < table.length; i++) {
                t.table[i] = this.table[i];
            }
            t.keySet = null;
            t.entrySet = null;
            t.values = null;
            t.modificaciones = 0;
            return t;
        }
        catch (CloneNotSupportedException e) {
            return null;
        }

    }

    /**
     * Determina si la tabla es igual al objeto especificado
     * @param obj el objeto a comparar con esta tabla
     * @return true si ambos objetos son iguales
     */
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
     * calculo de la clave hash del objeto Entry a partir de el hashcode de la clave ingresada
     * @param k hashcode de la clave ingresada
     * @return clave hash del objeto Entryve
     */
    private int h(int k){return h(k, this.table.length);}

    /**
     * calculo de la clave hash del objeto Entry a partir de la clave ingresada
     * @param key clave ingresada del objeto
     * @return clave hash del objeto Entry
     */
    private int h(K key){return h(key.hashCode(), this.table.length);}

    /**
     * calculo de la clave hash del objeto Entry
     * @param key clave ingresada del objeto
     * @param t longitud de la tabla
     * @return clave hash del objeto Entry
     */
    private int h(K key, int t){return h(key.hashCode(),t);}

    private int h(int k, int t){
        if(k < 0) k *=-1;
        return k %t;
    }


    /**
     * convierte los elementos de la tabla en string asignando un formato para que sea legible
     * @return la tabla en formato String
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("{");
        for(Map.Entry<K, V> e : this.entrySet()){
            s.append(e + ", ");
        }
        s.append("}");
        return s.toString();
    }

    /**
     * Una clase interna que guarda los pares de objetos que se almacenan en la tabla.
     * @param <K> el tipo de la key
     * @param <V> el tipo del valor
     */
    private class Entry<K,V> implements AbstractMap.Entry<K,V>{
        private K key;
        private V value;
        private boolean borrado;

        /**
         * Crea un objeto Entry con los parámetros especificados.
         * @param key representa la clave del objeto Entry
         * @param value representa el Valor del objeto Entry
         * @throws  java.lang.IllegalArgumentException si alguno de los dos parametros es null
         */
        public Entry(K key, V value){
            if(key ==null || value == null){
                throw new IllegalArgumentException("Entry(): parámetro null...");
            }
            this.key = key;
            this.value = value;
            this.borrado=false;
        }

        /**
         * Devuelve la clave del objeto
         * @return clave del objeto
         */
        @Override
        public K getKey() {
            return key;
        }

        /**
         * Devuelve el valor del objeto
         * @return valor del objeto
         */
        @Override
        public V getValue() {
            return value;
        }

        /**
         * Determina si el Entry tiene borrado logico
         * @return true si el Entry esta borrado
         */
        public boolean isBorrado() {
            return borrado;
        }

        /**
         * Setea el valor de un objeto pisando el que tenia antes
         * @param value nuevo valor del objeto
         * @return el valor que tenia antes el objeto
         */
        @Override
        public V setValue(V value) {
            if(value!=null) this.value=value;
            V viejo = this.value;
            this.value = value;
            return viejo;
        }

        /**
         * Determina si el Objeto entry es igual a uno ingresado por parametro
         * @param obj el objeto a comprar con el Entry
         * @return true si los objetos son iguales
         */
        public boolean equals(Object obj){
            if (this == obj) { return true; }
            if (obj == null) { return false; }
            if (this.getClass() != obj.getClass()) { return false; }

            final Entry other = (Entry) obj;
            if (!Objects.equals(this.key, other.key)) { return false; }
            if (!Objects.equals(this.value, other.value)) { return false; }
            return true;
        }

        /**
         * Calcula el codigo de has del Entry
         * @return codigo del hash del Entry
         */
        public int hashCode(){
            int hash = 7;
            hash = 61 * hash + Objects.hashCode(this.key);
            hash = 61 * hash + Objects.hashCode(this.value);
            return hash;
        }

        /**
         * Realiza un borrado sobre el Entry
         */
        public void borrar(){
            this.borrado=true;
        }

        /**
         * Convierte la clave y el valor del objeto a String
         * @return cadena que represneta al objeto con su clave y su valor
         */
        @Override
        public String toString() {
            return key + ": " + value;
        }
    }

    /**
     * Retorna un Set con las vistas de todas las claves
     * @return un objeto tipo KeySet de la tabla
     */
    @Override
    public Set<K> keySet() {
        if(keySet == null){
            keySet = new KeySet();
        }
        return keySet;
    }

    /**
     * Retorna una colección con las vistas de todos los valores
     * @return un objeto tipo ValueCollection de la tabla
     */
    @Override
    public Collection<V> values() {
        if(values == null){
            values = new ValueCollection();
        }
        return values;
    }

    /**
     * Retorna un set con todas las entradas que estan en la tabla
     * @return un objeto tipo EntrySet de la tabla
     */
    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        if(entrySet == null){
            entrySet = new EntrySet();
        }
        return entrySet;
    }


    /**
     * Clase interna que representa a todas las keys almacenadas en la tabla de hash
     */
    private class KeySet extends AbstractSet<K>{
        /**
         * Retorna un iterador para las keys
         * @return un iterador para las keys
         */
        @Override
        public Iterator<K> iterator() {
            return new KeySetIterator();
        }

        /**
         * Indica el tamaño que tiene la colección de claves.
         * @return
         */
        @Override
        public int size() {
            return TSBHashTableDA.this.cantidad;
        }

        /**
         * Permite conocer si el objeto indicado por parámetro está en la lista.
         * @param o objeto que se quiere saber si está en la lista.
         * @return true si el objeto se encuentra en la lista.
         */
        @Override
        public boolean contains(Object o) {
            return TSBHashTableDA.this.containsKey(o);
        }


        private class KeySetIterator implements Iterator<K>{
            private int cont, current, expected_modCount;
            /**
             * Crea un objeto iterador sobre la colección.
             */
            public KeySetIterator(){
                cont = 0;
                current = 0;
                expected_modCount= TSBHashTableDA.this.modificaciones;
            }
            /**
             * Permite saber si hay un siguiente elemento
             * @return true si todavía quedan elementos por recorrer.
             */
            @Override
            public boolean hasNext() {
                return (cont < size());
            }
            /**
             * Obtiene el proximo objeto key de la lista a partir del indice en el que esta ubicado
             * @return proximo objeto key
             */
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

    /**
     * Clase interna que representa la vista stateless de la tabla para operar sobre los values.
     */
    private class ValueCollection extends AbstractCollection<V>{
        /**
         * Retorna un iterador para los valores
         * @return un iterador para los valores
         */
        @Override
        public Iterator iterator() {
            return new ValueCollectionIterator();
        }
        /**
         * Indica el tamaño que tiene la colección de valores.
         * @return un entero que representa cuantos elementos hay
         */
        @Override
        public int size() {
            return TSBHashTableDA.this.size();
        }

        /**
         * Permite conocer si el objeto indicado por parámetro está en la lista.
         * @param o objeto que se quiere saber si está en la lista.
         * @return true si el objeto se encuentra en la lista.
         */
        @Override
        public boolean contains(Object o) {
            return TSBHashTableDA.this.containsValue(o);
        }

        @Override
        public void clear() {
            TSBHashTableDA.this.clear();
        }
        /**
         * Clase para iterar sobre los elementos de la colección de valores.
         */
        private class ValueCollectionIterator implements Iterator<V>{
            private int current, cont;
            /**
             * Crea un objeto iterador sobre la colección.
             */
            public ValueCollectionIterator(){
                current = 0;
                cont = 0;
            }
            /**
             * Permite saber si hay un siguiente elemento
             * @return true si todavía quedan elementos por recorrer.
             */
            @Override
            public boolean hasNext() {
                return (cont < TSBHashTableDA.this.size());
            }
            /**
             * Obtiene el proximo objeto valor de la lista a partir del indice en el que esta ubicado
             * @return proximo objeto valor
             */
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

    /**
     * Clase interna que representa la vista stateless de la tabla para operar sobre las Entradas.
     */
    private class EntrySet extends AbstractSet<Map.Entry<K,V>>{
        /**
         * Retorna un iterador para las entradas
         * @return un iterador para las entradas
         */
        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            return new EntrySetIterator();
        }

        /**
         * Indica el tamaño que tiene la colección de entradas.
         * @return un entero que representa el tamaño que tiene la colección.
         */
        @Override
        public int size() {
            return TSBHashTableDA.this.size();
        }
        /**
         * Permite conocer si hay un objeto pasado por parámetro en la tabla
         * @param o objeto que se quiere enocontrar.
         * @return true si el objeto está en la tabla.
         */
        public boolean contains(Object o){
            Entry entrada= (Entry) o;
            K clave = (K)entrada.getKey();
            return TSBHashTableDA.this.containsKey(clave);
        }

        /**
         * Clase para iterar sobre los elementos de la colección de entradas.
         */
        private class EntrySetIterator implements Iterator<Map.Entry<K,V>>{
            private int current, cont;

            /**
             * Crea un objeto iterador sobre la colección.
             */
            public EntrySetIterator(){
                current = 0;
                cont = 0;
            }

            /**
             * Permite saber si hay un siguiente elemento
             * @return true si todavía quedan elementos por recorrer.
             */
            @Override
            public boolean hasNext() {
                return (cont < TSBHashTableDA.this.size());
            }

            /**
             * Obtiene el proximo objeto Entry de la lista a partir del indice en el que esta ubicado
             * @return proximo objeto Entry
             */
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
