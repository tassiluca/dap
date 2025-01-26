mod foo_bindings;

use foo_bindings::*; // Import the generated bindings

#[repr(C)]
pub struct State {
    x: i32,
    y: i32,
}

fn main() {
    // Mimicking the struct creation in C
    let foo = State {
        x: 1,
        y: 2,
    };

    // Access the fields and print them
    println!("foo.x = {}", foo.x);
    println!("foo.y = {}", foo.y);
}
