package com.at.cancerbero.server.model;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

import lombok.Data;

@Data
public class Node {
    private String id;
    private String name;
    private Date lastPing;
}

//{
//        "id": "1234567890",
//        "lastPing": "2012-12-12T22:00:01",
//        "modules": {
//        "alarm": {
//        "pins": {
//        "A0": {
//        "activations": {
//        "timestamp": "2012-12-12T22:00:40",
//        "value": "137"
//        },
//        "id": "A0",
//        "input": "digital",
//        "mode": "low",
//        "readings": {
//        "timestamp": "2012-12-12T22:00:41",
//        "value": "138"
//        },
//        "threshold": 0,
//        "type": "sensor"
//        }
//        },
//        "status": {
//        "source": "me",
//        "timestamp": "2012-12-12T22:00:50",
//        "value": "IDLE"
//        }
//        }
//        },
//        "name": "Cocina"
//        }
