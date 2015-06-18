package ph.edu.speed.orbit.pixelcam;

import java.util.HashMap;

/**
 * Created by jaredsalazar on 6/16/15.
 */
public class pixelcomparator {
    static int max,temp;

    public pixelcomparator(int[] colors) {

        HashMap<Integer,Integer> hm=new HashMap<Integer,Integer>();
        max = 1;
        temp = 0;

        for(int i=0;i<colors.length;i++)
        {
            if(hm.get(colors[i])!=null)
            {int count=hm.get(colors[i]);
                count=count+1;
                hm.put(colors[i],count);
                if(count>max)
                {max=count;
                    temp=colors[i];}
            }
            else
            {hm.put(colors[i],1);}
        }
    }
}
