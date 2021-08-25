using System;
using System.IO;
//9.10.19: Unity kann das nicht einbinden. Kann man laut diverser Foren
//selber durch kopieren der dll machen, scheint aber fragwuerdig.
//using System.Drawing;
using UnityEngine;
using java.lang;
using de.yard.threed.core.platform;
using de.yard.threed.core;

namespace de.yard.threed.platform.unity
{
   
    public class UnityCanvas  :  NativeCanvas
    {
        //Bitmap bmp;
	public ImageData image;

        static Log logger = PlatformUnity.getInstance ().getLog (typeof(UnityCanvas));
        public Texture2D texture;

        public UnityCanvas (int width, int height)
        {
		//System.Drawing finder er nicht
   /*bmp = new Bitmap(width, height);
using (Graphics g = Graphics.FromImage(bmp))
{
    g.DrawLine(new Pen(Color.Red), 0, 0, 10, 10);
}*/
		image = new ImageData(width,height);
        }


    }
}
