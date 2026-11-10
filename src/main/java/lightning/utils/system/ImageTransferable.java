package lightning.utils.system;

import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.util.Collections;

public class ImageTransferable implements Transferable {
    private final Image image;
    private final File file;
    private final boolean includeFile;

    public ImageTransferable(Image arg0, File arg1, boolean arg2) {
        this.image = arg0;
        this.file = arg1;
        this.includeFile = arg2;
    }

    public DataFlavor[] getTransferDataFlavors() {
        if (!(!this.includeFile) && this.file != null) {
            var tmp0 = new DataFlavor[2];
            tmp0[0] = DataFlavor.imageFlavor;
            tmp0[1] = DataFlavor.javaFileListFlavor;
            return tmp0;
        }
        var tmp1 = new DataFlavor[1];
        tmp1[0] = DataFlavor.imageFlavor;
        return tmp1;
    }

    public boolean isDataFlavorSupported(DataFlavor arg0) {
        if (DataFlavor.imageFlavor.equals(arg0)) return this.image != null;
        if ((!this.includeFile) || (this.file == null)) break label_3;
        return DataFlavor.javaFileListFlavor.equals(arg0);
    }

    public Object getTransferData(DataFlavor arg0) throws UnsupportedFlavorException {
        if (DataFlavor.imageFlavor.equals(arg0) && this.image != null) return this.image;
        if (!(!this.includeFile) && this.file != null && DataFlavor.javaFileListFlavor.equals(arg0)) return Collections.singletonList(this.file);
        throw new UnsupportedFlavorException(arg0);
    }

}
