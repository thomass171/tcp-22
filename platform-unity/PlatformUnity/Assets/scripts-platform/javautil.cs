using System;
using java.lang;
using de.yard.threed.engine;
using de.yard.threed.core;

namespace java.util
{
    public class ArrayList<T> : List <T>
    {
        public ArrayList ()
        {
        }

        public ArrayList (int size):        base(size)
        {
    
        }

        public ArrayList (List<T> l)
        {
            this.list = ((ArrayList<T>)l).list;
        }

        public ArrayList (Set<T> s)
        {
            this.list = s.list;
        }

        public ArrayList(System.Collections.Generic.List<T> l){
            this.list = l;
        }
    }

    public class List<T> :  Collection<T>
    {
        // die C# ArrayList ist veraltet. public, weil das in der Platform fuer toarray() schonmal praktisch ist. Zumindest vorerst.
        public System.Collections.Generic.List<T> list = new System.Collections.Generic.List<T> ();

        public List(){
        }

        public List(int size){
            //TODO size
        }

        public List(System.Collections.Generic.List<T> l){
            this.list = l;
        }

        public void Reset ()
        {
            Util.notyet ();
        }

        public int size ()
        {
            return list.Count;
        }

        /*public T get (int index)
        {
            return list [index];
        }*/

        public T get (Nullable<Int32> Index)
        {
            int index = Index.Value;
            return list [index];
        }

        public void add (T item)
        {
            list.Add (item);
        }

        public void add (int index, T item)
        {
            list.Insert (index, item);
        }

        public void clear ()
        {
            list.Clear ();
        }

        public void addAll (System.Collections.Generic.IEnumerable<T> l)
        {
            list.AddRange (l);
        }

        public void remove (T item)
        {
            list.Remove (item);
        }

        public void removeAll (System.Collections.Generic.IEnumerable<T> l)
        {
            foreach (T t in l) {
                list.Remove(t);
            }
            //list.RemoveRange (l);
        }

        public bool contains (T item)
        {
            return list.Contains (item);
        }

        public List<T> subList (int fromIndex,
            int toIndex)
        {
            return new ArrayList<T>(list.GetRange (fromIndex, toIndex - fromIndex));           
        }

        public void set (int pos, T item)
        {
            list [pos] = item;
        }

        public T remove (int pos)
        {
            T ri = list [pos];
            list.RemoveAt (pos);
            return ri;
        }

        public T[] toArray(T[] sample){
            return list.ToArray ();
            //T[] arr = new T[
        }

        public int indexOf(T o){
            return list.IndexOf (o, 0, list.Count);
        }

        public System.Collections.Generic.IEnumerator<T> GetEnumerator ()
        {
            for (int i = 0; i < list.Count; i++)
                yield return list [i];
        }

        public bool isEmpty(){
            return list.Count == 0;
        }

        // Explicit for IEnumerable because weakly typed collections are Bad
        System.Collections.IEnumerator System.Collections.IEnumerable.GetEnumerator ()
        {
            // uses the strongly typed IEnumerable<T> implementation
            return this.GetEnumerator ();
        }
        // 20.7.17 C# kann keine Iterator wie Java
        /* public Iterator<T> iterator ()
        {
            return new SimpleIterator<T>(this.iterator ());
        }*/

        public void sort(Comparison<T> comparator)
        {
            list.Sort(comparator);
        }

    }

    public class HashMap<T,T2> : Map <T,T2>
    {
        

    }

    /**
     * TreeMap for MT safety. TODO ConcurrentDictionary statt Dictionary verwenden.
     */
    public class TreeMap<T,T2> : Map <T,T2>
    {


    }

    /**
     * Java allows one null value as key in a hashmap. C# dictionary doesn't. But there is no use case for doing so.
     * 26.6.20 now it is. TODO test. 
     */
    public class Map<T,T2> : System.Collections.Generic.Dictionary<T,T2> /*System.Collections.IDictionary*/
    {
        //System.Collections.Generic.Dictionary<T,T2> map = new  System.Collections.Generic.Dictionary<T,T2>();
        Boolean hasNullKey = false;
        T2 valueForNullKey;

        public void put (T key, T2 value)
        {
            if (key == null) {
                hasNullKey = true;
                valueForNullKey = value;
                return;
            }
            //Methode add ueberschreibt keine existing
            this[key] = value;
            //Add (key, value);
        }

        /**
         * virtual damit man overriden kann
         */
       virtual public T2 get (T key)
        {
            T2 item;

            if (key == null) {
                return valueForNullKey;//default(T2);
            }
            bool found = TryGetValue (key, out item);
            if (!found) {
                return default(T2);
            }
            return item;
        }

        public int size ()
        {
            int size = Count;
            if (hasNullKey) {
                size++;
            }
            return size;
        }

        public Set<T> keySet ()
        {
            Set<T> set = new SimpleSet<T> (new System.Collections.Generic.List<T> (Keys));
            if (hasNullKey) {
                set.add (default (T));
                //throw new System.Exception("no null in keyset");
            }
            return set;
        }

        public void remove (T key)
        {
            if (key == null) {
                hasNullKey = false;
                valueForNullKey = default(T2);
                return;
            }
            this.Remove (key);
        }

        public void clear()
        {
            this.Clear ();
            hasNullKey = false;
            valueForNullKey = default (T2);
        }

        public bool isEmpty()
        {
            return Count == 0 && hasNullKey==false;
        }

        public System.Collections.Generic.List<T2> values()
        {
            System.Collections.Generic.List<T2> l= new System.Collections.Generic.List<T2> (Values);
            if (hasNullKey) {
                l.Add (valueForNullKey);
            }
            return l;
        }

        public bool containsKey(T o)
        {
            if (o==null && hasNullKey) {
                return true;
            }
            return this.ContainsKey (o);
        }
    }

    public interface Iterator<E>
    {
        bool hasNext ();

        E next ();

        void remove ();
    }

    public class Set<T> : ArrayList<T>, Collection<T>
    {
        public Set (System.Collections.Generic.List<T> keylist)
        {
            list = keylist;
        }

        /*
         * 20.7.17: C# kann keine Iterator wie Java
         */
        public Iterator<T> iterator()
        {
            Util.notyet ();
            return null;//list.GetEnumerator ();
        }
    }

    /**
	 * Gibts in Java so nicht
	 */
    public class SimpleSet<T> : Set<T>
    {
        public SimpleSet (System.Collections.Generic.List<T> keylist) : base (keylist)
        {
			
        }

    }

    public interface Collection<T> : System.Collections.Generic.IEnumerable<T>
    {
    }

    public class Vector<T> : ArrayList <T>
    {
    }
        
    /*
     * 20.7.17: C# kann keine Iterator wie Java
     */
    /*public class SimpleIterator<T> : Iterator<T>
    {
        System.Collections.IEnumerator iter;

        SimpleIterator(System.Collections.IEnumerator iterator){}

        bool hasNext (){
            iter.
        }

        T next (){
        }

        void remove (){
            iter.
        }

    }*/
}

