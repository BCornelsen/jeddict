//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2014.01.21 at 01:52:19 PM IST
//
package io.github.jeddict.jpa.spec;

import static io.github.jeddict.jcode.JPAConstants.GENERATED_VALUE_FQN;
import io.github.jeddict.source.AnnotatedMember;
import io.github.jeddict.source.AnnotationExplorer;
import io.github.jeddict.source.JavaSourceParserUtil;
import java.util.Optional;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import org.eclipse.persistence.internal.jpa.metadata.sequencing.GeneratedValueMetadata;

/**
 *
 *
 * @Target({METHOD, FIELD}) @Retention(RUNTIME) public @interface GeneratedValue
 * { GenerationType strategy() default AUTO; String generator() default ""; }
 *
 *
 *
 * <p>
 * Java class for generated-value complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="generated-value">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="strategy" type="{http://java.sun.com/xml/ns/persistence/orm}generation-type" />
 *       &lt;attribute name="generator" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "generated-value")
public class GeneratedValue {

    @XmlAttribute
    protected GenerationType strategy;
    @XmlAttribute
    protected String generator;

    @Deprecated
    public static GeneratedValue load(Element element, VariableElement variableElement) {
        AnnotationMirror annotationMirror = JavaSourceParserUtil.findAnnotation(element, GENERATED_VALUE_FQN);
        GeneratedValue generatedValue = null;
        if (annotationMirror != null) {
            generatedValue = new GeneratedValue();
            Object strategyObj = JavaSourceParserUtil.findAnnotationValue(annotationMirror, "strategy");
            if (strategyObj != null) {
                generatedValue.strategy = GenerationType.valueOf(strategyObj.toString());
            }
            generatedValue.generator = (String) JavaSourceParserUtil.findAnnotationValue(annotationMirror, "generator");
        }
        return generatedValue;

    }

    public static GeneratedValue load(AnnotatedMember member) {
        GeneratedValue generatedValue = null;
        Optional<AnnotationExplorer> generatedValueOpt = member.getAnnotation(javax.persistence.GeneratedValue.class);
        if (generatedValueOpt.isPresent()) {
            generatedValue = new GeneratedValue();
            AnnotationExplorer annotation = generatedValueOpt.get();
            annotation.getString("generator").ifPresent(generatedValue::setGenerator);
            generatedValue.setStrategy(
                    annotation.getEnum("strategy")
                    .map(GenerationType::valueOf)
                    .orElse(GenerationType.DEFAULT)
            );
        }
        return generatedValue;
    }

    /**
     * Gets the value of the strategy property.
     *
     * @return possible object is {@link GenerationType }
     *
     */
    public GenerationType getStrategy() {
        return strategy;
    }

    /**
     * Sets the value of the strategy property.
     *
     * @param value allowed object is {@link GenerationType }
     *
     */
    public void setStrategy(GenerationType value) {
        this.strategy = value;
    }

    /**
     * Gets the value of the generator property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getGenerator() {
        return generator;
    }

    /**
     * Sets the value of the generator property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setGenerator(String value) {
        this.generator = value;
    }
    
    public GeneratedValueMetadata getAccessor() {
        GeneratedValueMetadata accessr = new GeneratedValueMetadata();
        accessr.setGenerator(getGenerator());
        accessr.setStrategy(getStrategy().name());
        return accessr;
    }

}
