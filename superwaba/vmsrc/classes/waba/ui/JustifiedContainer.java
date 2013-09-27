package waba.ui;

import waba.ui.*;
import waba.fx.*;
import waba.sys.*;
import waba.util.*;

/** used to make a bit easy to place controls in a container. The controls must be inserted in horizontal order. 
    isnt necessary to fill all the specified number of cols.
    example:
    <pre>
      JustifiedContainer jc = new JustifiedContainer();
      jc.setColumnCount(2);
      jc.setGaps(0,6,0); // 6 pixels of horizontal gap
      jc.setJustify(0,RIGHT,TOP); // justify column 0 at right
      jc.add(0,new Label("Please fill the fields",Label.CENTER),0,0); // container width and preferred height
      jc.add(0,new Label("Name:"),70,0); 
      jc.add(1,new Edit(),0,0); // fill column 1 to the end
      jc.add(0,new Label("Address:"),70,0); 
      jc.add(1,new Edit(),0,0);
    </pre>
*/
public class JustifiedContainer extends Container
{
   class Column
   {
      /** gaps horizontal e vertical */
      public int gapX=2,gapY=3;
      /** justificacao para a coluna */
      public int justX=LEFT,justY=TOP;
      /** ultima coordenadas inseridas */
      public int lastX=0,lastY=-1,lastW=0,lastH=0;
      /** vetor de controles desta coluna */
      public Vector controls = new Vector(5);
   }   
   /** numero de colunas do container */
   protected int columnCount=1;
   /** colunas */
   protected Column columns[];
   //////////////////////////////////////////////////////////////////////////
   public JustifiedContainer()
   {
   }
   //////////////////////////////////////////////////////////////////////////
   /** initialize the columns */
   public void setColumnCount(int numcols)
   {
      if (numcols == 0) numcols = 1;
      this.columnCount = numcols;
      this.columns = new Column[columnCount];
      for (int i =0; i < columnCount; i++)
         columns[i] = new Column();
   }
   //////////////////////////////////////////////////////////////////////////
   /** set the justifies of the column <col>. can be LEFT, RIGHT, TOP, BOTTOM, CENTER */
   public void setJustify(int col, int justX, int justY)
   {
      columns[col].justX = justX;
      columns[col].justY = justY;
   }
   //////////////////////////////////////////////////////////////////////////
   /** set the gaps for the column <col> */
   public void setGaps(int col, int gapX, int gapY)
   {
      columns[col].gapX = gapX;
      columns[col].gapY = gapY;
   }
   //////////////////////////////////////////////////////////////////////////
   /** adds the control.
   @param control the specified control.
   @param col which column to add
   @param controlW if greater than 0: width of the control; if 0: fill to the end of the container width; if PREFERRED: preferred control width 
   @param controlH if greater than 0: height of the control; if 0: default line height; if PREFERRED: preferred control height
   */
   public void add(int col, Control control, int controlW, int controlH)
   {
      super.add(control);
      if (columns == null) setColumnCount(1);
      Column c = columns[col];
      c.controls.add(control);
      
      int leftColumnX = col==0?0:(columns[col-1].lastX+columns[col-1].lastW);
      if (controlW == 0) controlW = col==0?width:(width-leftColumnX-c.gapX); else
      if (controlW == PREFERRED) controlW = control.getPreferredWidth();

      if (controlH == 0) controlH = fm.getHeight(); else
      if (controlH == PREFERRED) controlH = control.getPreferredHeight();
      
      int controlX=0,controlY=0;
      if (col == 0)
         switch (c.justY)
         {
            case TOP   : controlY = c.lastY==-1?0:(c.lastY+c.lastH+c.gapY); break;
            case BOTTOM: controlY = (c.lastY==-1?height:(c.lastY-c.gapY))-controlH; break;
         }
      else
      {
         controlY = columns[0].lastY;
         if (controlH < columns[0].lastH) // se menor que a linha, centraliza o controle
            controlY += (columns[0].lastH-controlH)/2;
      }   
      if (col > 0 && controlY != c.lastY) c.lastX = 0; // mudou de linha, zera o cara
      switch (c.justX)
      {
         case LEFT  : controlX = leftColumnX!=0?(leftColumnX+c.gapX):0; break;
         case RIGHT : controlX = (c.lastX==0?width:(c.lastX-c.gapX)) - controlW; break;
         case CENTER: controlX = (width - leftColumnX - controlW)/2; break;
      }
      control.setRect(controlX,controlY,controlW,controlH);
      c.lastX = controlX;
      c.lastY = controlY;
      c.lastW = controlW;
      c.lastH = controlH;
   }
   //////////////////////////////////////////////////////////////////////////
}