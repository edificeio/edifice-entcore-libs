package org.entcore.common.s3.dataclasses;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="CompleteMultipartUpload")
public class CompleteMultipartUpload {

    @XmlElement(name="Part")
	protected List<CompletePart> parts;

    public CompleteMultipartUpload() {
        this.parts = new ArrayList<>();
    }

    public CompleteMultipartUpload(List<CompletePart> parts) {
        this.parts = parts;
    }

    public List<CompletePart> getParts() {
        return parts;
    }

    public void setParts(List<CompletePart> parts) {
        this.parts = parts;
    }

    public void addPart(CompletePart part) {
        parts.add(part);
    }
}
