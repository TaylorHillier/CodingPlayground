
import './App.css'
import {Routes, Route, Link, useParams} from "react-router-dom";
import {useState, useEffect} from "react";

interface User
{
  name: string;
  id: number;
  email: string;
}

function HomePage()
{
  return <h1>Homepage</h1>
}

function AboutPage()
{
  return <h1>About</h1>
}

function ContactPage()
{
  return <h1>Contact</h1>
}

function SingleUserPage()
{
  const {id} = useParams<{id: string}>();

  const [user, setUser] = useState<User | null>(null);
  const [isUserLoading, setUserLoading] = useState<boolean>(true);
   const [userError, setErrorMessage] = useState<string | null>(null);
    
  useEffect(() => {

    if(!id) {return;};

    async function fetchUsers()
    {
      try {
        setUserLoading(true);
        const userResponse = await fetch(
           `https://jsonplaceholder.typicode.com/users/${id}`
        )

        if(!userResponse.ok)
        {
          throw new Error("failed to fetch users");
        }

        const userData = await userResponse.json();
        setUser(userData);
      }
      catch(error : any)
      {
        setErrorMessage(error.message||"unknown error");
      } 
      finally
      {
        setUserLoading(false);
      }
    }

    fetchUsers();
  }, [id]);

   if(isUserLoading)
  {
    return "Loading";
  }

  if(userError)
  {
    return <div>{userError}</div>;
  }


    return (
    <div>
      {user?.name} ({user?.email})
    </div>
  );
}

function UserPage()
{
  const [userList, setUserList] = useState<User[]>([]);
  const [isUserLoading, setUserLoading] = useState(true);
  const [userListError, setErrorMessage] = useState(null);

  useEffect(() => {
    async function fetchUsers()
    {
      try {
        setUserLoading(true);
        const userResponse = await fetch(
           "https://jsonplaceholder.typicode.com/users"
        )

        if(!userResponse.ok)
        {
          throw new Error("failed to fetch users");
        }

        const userData = await userResponse.json();
        setUserList(userData);
      }
      catch(error : any)
      {
        setErrorMessage(error.message||"unknown error");
      } 
      finally
      {
        setUserLoading(false);
      }
    }

    fetchUsers();
  }, [])

  if(isUserLoading)
  {
    return "Loading";
  }

  if(userListError)
  {
    return <div>{userListError}</div>;
  }

  return (
    <div>
      <ul>
        {userList.map((user) => (
          <li key={user.id}>
            {user.name} ({user.email})
            <Link to={`/users/${user.id}`}>Users</Link>
          </li>
        ))}
      </ul>
    </div>
  );
}

function App() {

  return (
    <div>
      <header>
        <h1>Example SPA</h1>
      </header>

      <nav>
        <Link to="/">Home</Link>
        <Link to="/about">About</Link>
        <Link to="/users">Users</Link>
        <Link to="/contact">Contact</Link>
      </nav>

      <main>
        <Routes>
          <Route path="/" element={<HomePage/>}/>
          <Route path="/about" element={<AboutPage/>}/>
          <Route path="/users" element={<UserPage/>}/>
           <Route path="/users/:id" element={<SingleUserPage/>}/>
          <Route path="/contact" element={<ContactPage/>}/>

        </Routes>
      </main>
    </div>
  )
}

export default App
