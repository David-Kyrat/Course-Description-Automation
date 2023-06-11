/* #![allow(dead_code, unused_imports)]

use std::future::Future;
use std::time::Duration;
use std::path::*;
use futures;
use tokio;  

struct Item {
    pub resolved: bool,
    pub i: u8,
}

pub const REPO: &str =
    "https://raw.githubusercontent.com/David-Kyrat/Course-Description-Automation/master";
/// Resolves (as string) given `rel_path` agains the url (as string) of the repo given by `repo()`
/// # Returns
/// new resolved url (as string) i.e. `repo()/rel_path`
pub fn rls(rel_path: &str) -> String {
    format!("{REPO}/{rel_path}")
}



impl Item {
    async fn resolve(&mut self) {
        tokio::time::sleep(Duration::from_millis(1)).await;
        self.resolved = true;
    }

    fn print_result(&self) {
        println!("[{}] - resolved={}", self.i, self.resolved);
    }
}

async fn rl(item: &mut Item) {
    tokio::time::sleep(Duration::from_millis(1)).await;
    item.resolved = true;
}

fn pr(item: &Item) {
    println!("[{}] - resolved={}", item.i, item.resolved);
}

async fn join_parallel<T: Send + 'static>(
    futs: impl IntoIterator<Item = impl Future<Output = T> + Send + 'static>,
) -> Vec<T> {
    let tasks: Vec<_> = futs.into_iter().map(tokio::spawn).collect();
    futures::future::join_all(tasks)
        .await
        .into_iter()
        .map(Result::unwrap)
        .collect()
}

fn pto<T>(_: &T) {
    eprintln!("{}", std::any::type_name::<T>())
}

pub async fn dl(
    url: &str,
) -> std::io::Result<String> {
    let p = std::env::current_dir()?;
    Ok(format!("Downloading {REPO}/{url} to {:#?}", p))
}


#[tokio::main]
async fn main() {
    let _items: Vec<_> = (0..10)
        .map(|idx| Item {
            resolved: false,
            i: idx,
        })
        .collect();

    /*let tasks: Vec<_> = items
        .into_iter()
        .map(|mut item| async move {
            //.resolve().await;
            rl(&mut item).await;
            pr(&item);
        })
        .map(tokio::spawn)
        .collect();
    pto(&tasks);

    let xs = futures::future::join_all(tasks).await; // doesnt check which succeeded or not
    dbg!(&xs);
    pto(&xs);*/

    println!("--------------------");

    /*let items: Vec<_> = (0..10)
        .map(|idx| Item {
            resolved: false,
            i: idx,
        })
        .collect();*/

    let resources_to_dl = vec![
        "files/res/logging_config.yaml",
        "files/res/2",
        "files/res/3",
        "files/res/4",
        "files/res/5",
        "files/res/6",
        "files/res/7",
    ];
 
    let _results = join_parallel(resources_to_dl.into_iter().map(|rel_path| async move {
        dl(rel_path)
    })).await;
    
    /*let items = join_parallel(items.into_iter().map(|mut item| async move {
        //.resolve().await;
        rl(&mut item).await;
        pr(&item);
        item;
    }))
    .await;
    dbg!(&items);*/
    /*for item in &items {
        item.print_result();
    }*/
}
*/
