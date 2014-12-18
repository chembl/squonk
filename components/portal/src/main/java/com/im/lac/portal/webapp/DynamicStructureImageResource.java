package com.im.lac.portal.webapp;

import chemaxon.formats.MolImporter;
import chemaxon.marvin.MolPrinter;
import chemaxon.struc.Molecule;
import com.im.lac.portal.service.DatasetRow;
import com.im.lac.portal.service.ListDatasetRowFilter;
import com.im.lac.portal.service.PrototypeService;
import com.im.lac.portal.service.PrototypeServiceMock;
import org.apache.wicket.request.resource.DynamicImageResource;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.logging.Logger;

public class DynamicStructureImageResource extends DynamicImageResource {

    private static final Logger logger = Logger.getLogger(DynamicStructureImageResource.class.getName());
    private static final Rectangle RECTANGLE = new Rectangle(200, 130);
    @Inject
    private PrototypeService service;

    @Override
    protected void setResponseHeaders(ResourceResponse data, Attributes attributes) {
        // this disables some unwanted default caching
    }

    @Override
    protected byte[] getImageData(Attributes attributes) {
        String datasetIdAsString = attributes.getParameters().get("datasetIdAsString").toString();
        String rowIdAsString = attributes.getParameters().get("rowIdAsString").toString();
        try {
            return renderStructure(datasetIdAsString, rowIdAsString);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected String loadStructureData(String datasetIdAsString, String rowIdAsString) {
        String structureData = null;
        Long datasetId = Long.valueOf(datasetIdAsString);
        Long rowId = Long.valueOf(rowIdAsString);

        ListDatasetRowFilter listDatasetRowFilter = new ListDatasetRowFilter();
        listDatasetRowFilter.setDatasetid(datasetId);
        java.util.List<DatasetRow> datasetRowList = service.listDatasetRow(listDatasetRowFilter);
        DatasetRow matchDatasetRow = null;
        for(DatasetRow datasetRow : datasetRowList) {
            if(datasetRow.getId().equals(rowId)) {
                matchDatasetRow = datasetRow;
            }
        }
        if(matchDatasetRow != null) {
            structureData = (String) matchDatasetRow.getProperty(PrototypeServiceMock.STRUCTURE_FIELD_NAME);
        }
        return structureData;
    }

    protected Rectangle getRectangle() {
        return RECTANGLE;
    }

    protected Molecule getMolecule(String datasetIdAsString, String rowIdAsString) throws Exception {
        String structureAsString = loadStructureData(datasetIdAsString, rowIdAsString);
        Molecule molecule = MolImporter.importMol(structureAsString);
        molecule.dearomatize();
        return molecule;
    }

    private byte[] renderStructure(String datasetIdAsString, String rowIdAsString) throws Exception {
        String fine = "Dynamically rendering structure " + datasetIdAsString + "/" + rowIdAsString;
        logger.fine(fine);
        MolPrinter molPrinter = new MolPrinter();
        molPrinter.setMol(getMolecule(datasetIdAsString, rowIdAsString));

        BufferedImage image = new BufferedImage(getRectangle().width, getRectangle().height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = image.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        graphics2D.setColor(Color.white);
        graphics2D.fillRect(0, 0, getRectangle().width, getRectangle().height);
        double scale = molPrinter.maxScale(getRectangle());
        molPrinter.setScale(scale);
        molPrinter.paint(graphics2D, getRectangle());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", outputStream);
        return outputStream.toByteArray();
    }
}
