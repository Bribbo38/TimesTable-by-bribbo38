//
//  Item.swift
//  TimesTable by numb
//
//  Created by Federico Musso on 18/02/26.
//

import Foundation
import SwiftData

@Model
final class Item {
    var timestamp: Date
    
    init(timestamp: Date) {
        self.timestamp = timestamp
    }
}
