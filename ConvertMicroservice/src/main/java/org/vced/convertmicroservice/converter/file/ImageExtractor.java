package org.vced.convertmicroservice.converter.file;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.pdfbox.contentstream.PDFStreamEngine;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



@NoArgsConstructor
class ImageExtractor extends PDFStreamEngine {
    public final List<BufferedImage> IMAGE_BUFFER = new ArrayList<>();
    private float yPosition = 0; // в пунктах (1/72 дюйма)
    @Getter
    private final List<Float> imageYPositions = new ArrayList<>();
    // в процентах (0 - самый низ, 1 - самый верх страницы), чтобы не мешала разная высота разных страниц


    /**
     * @param operator The operation to perform.
     * @param operands The list of arguments.
     * @throws IOException If there is an error processing the operation.
     */
    @Override
    protected void processOperator(Operator operator, List<COSBase> operands) throws IOException {
        String operation = operator.getName();
        if ("Do".equals(operation)) {
            COSName objectName = (COSName) operands.getFirst();
            PDXObject xobject = getResources().getXObject(objectName);
            if (xobject instanceof PDImageXObject image) {
                // save image to local
                BufferedImage bImage = image.getImage();
                IMAGE_BUFFER.add(bImage);

                float pageHeight = getCurrentPage().getMediaBox().getHeight();
                float yPosPercentage = 0;
                try {
                    yPosPercentage = yPosition / pageHeight;
                } catch (ArithmeticException e) {
                    // деление на ноль страшна
                }
                imageYPositions.add(yPosPercentage);
            }
        }
//      ниже плохой способ доставать координаты
//      оператор "cm" *изменяет* матрицу положения, а не показывает её финальное значение
//      если он всего один (как часто бывает) то он содержит актуальные координаты,
//      но иногда коорднаты будут неверны (хоть и на одной странице)
//      правильно будет при операторе "Do" делать следующее (из официальных примеров Apache):
//        Matrix ctm = getGraphicsState().getCurrentTransformationMatrix().clone();
//        float yPosition = ctm.getTranslateY();
//        System.out.println("CTM: " + ctm + ", translateY: " + ctm.getTranslateY());
//        imageYPositions.add(yPosition);
//      но я потратил где-то день и не смог понять почему он выдаёт нули и как это исправить
//      i hate adobe
        else if ("cm".equals(operator.getName())) {
            this.yPosition = ((COSNumber)operands.get(5)).floatValue();
        }
        else {
            super.processOperator(operator, operands);
        }
    }

    // Добавляем метод для очистки позиций
    public void clearPositions() {
        imageYPositions.clear();
    }

}